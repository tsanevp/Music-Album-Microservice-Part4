import Controller.AlbumController;
import Service.MySQLService;
import Service.RedisService;
import Util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
public class AlbumsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private ImageClient imageClient;

    private MySQLService mySQLService;
    private HikariDataSource connectionPool;
    private AlbumController albumController;

    private RedisService redisService;
    private JedisPool redisConnectionPool;

    @Override
    public void init() {
        this.imageClient = new ImageClient();
        this.albumController = new AlbumController();
        this.mySQLService = new MySQLService(Constants.MIN_MYSQL_CONNECTIONS, Constants.MAX_MYSQL_CONNECTIONS);
        this.connectionPool = this.mySQLService.getConnectionPool();

        this.redisService = new RedisService();
        this.redisConnectionPool = this.redisService.getConnectionPool();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");

        String urlPath = req.getPathInfo();
        String servletPath = req.getServletPath();

        // Check we have an empty url, the servlet path was called and that POST is multipart form
        if (urlPath != null || !isUrlValid(servletPath)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("invalid request");
            return;
        }

        Part image = req.getPart("image");
        Part albumProfilePart = req.getPart("profile");

        // Check we have a valid image part and album profile
        if (image == null || !isImageContentType(image.getContentType()) || albumProfilePart == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid or missing image OR missing album profile");
            return;
        }

        // Parse album profile data
        String[] albumProfileParsed = parseAlbumProfile(albumProfilePart);
        String artist = albumProfileParsed[0];
        String title = albumProfileParsed[1];
        String year = albumProfileParsed[2];

        // Ensure valid album profile data was passed
        if (artist == null || title == null || year == null) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Unable to parse album info");
            return;
        }

        // Create UUID and get image size
        String uuid = String.valueOf(UUID.randomUUID());
        String imageSize = String.valueOf(image.getSize());

        // Create image data and profile json strings
        String imageData = gson.toJson(new ImageMetaData().albumID(uuid).imageSize(imageSize));
        String albumProfile = gson.toJson(new AlbumsProfile().artist(artist).title(title).year(year));

        // Post image info and album profile to db
        try (Connection connection = this.connectionPool.getConnection()) {
            int rowsAffected = this.albumController.postToDatabase(connection, uuid, imageData, albumProfile);

            // This is part of our experimental phase to see if cache will improve GET throughput
            try (Jedis jedisConnection = this.redisConnectionPool.getResource()) {
                Pipeline pipeline = jedisConnection.pipelined();
                pipeline.hset(uuid, "NumberOfLikes", "0");
                pipeline.hset(uuid, "NumberOfDislikes", "0");
                pipeline.expire(uuid, Constants.REDIS_EXPIRE_TIME);
                pipeline.sync();
            } catch (Exception ignored) {
                // Try to store in cache, if it fails, do nothing since it is stored in main MySQL DB
            }

            if (rowsAffected > 0) {
                // Upload image to S3 cloud object storage
                this.imageClient.uploadImage(image, uuid);

                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write(imageData);
            } else {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.getWriter().write("Error when posting to database");
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("There was an error with the database");
        }
    }

    /**
     * Parse the album profile to extract the artist, title, and year information from the form part.
     *
     * @param albumProfilePart - The albumProfile details in multipart form format.
     * @return - A string array of the album profile information.
     * @throws IOException - If there was an error reading the input stream.
     */
    private String[] parseAlbumProfile(Part albumProfilePart) throws IOException {
        // Read the input stream as a string
        String jsonContent = new String(albumProfilePart.getInputStream().readAllBytes());

        // Define regular expressions to capture the key-value pairs
        String artistRegex = "artist: (.*?)\\n";
        String titleRegex = "title: (.*?)\\n";
        String yearRegex = "year: (.*?)\\n";

        // Extract values using regular expressions
        String artist = extractValue(jsonContent, artistRegex);
        String title = extractValue(jsonContent, titleRegex);
        String year = extractValue(jsonContent, yearRegex);

        return new String[]{artist, title, year};
    }

    /**
     * Uses the passed regex pattern to pull album info from the string.
     *
     * @param input - The current line of the album profile string.
     * @param regex - The regex pattern to search for.
     * @return - The album profile attribute if a match is found, else return null.
     */
    private String extractValue(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * Method to check an image file was sent in POST request.
     *
     * @param contentType - The content type of the form part. Must be an image.
     * @return - True if the content type is not empty and an image, false otherwise.
     */
    private boolean isImageContentType(String contentType) {
        return contentType != null && contentType.startsWith("image/");
    }

    /**
     * Method to return whether the path provided is an expected endpoint.
     *
     * @param urlPath - The current endpoint being evaluated.
     * @return true if the url is a valid endpoint, false otherwise.
     */
    private boolean isUrlValid(String urlPath) {
        for (Endpoint endpoint : Endpoint.values()) {
            Pattern pattern = endpoint.pattern;

            if (pattern.matcher(urlPath).matches()) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void destroy() {
        this.imageClient.shutDown();
        this.mySQLService.close();
        this.redisService.close();
    }

    /**
     * Enum constants that represent different possible endpoints
     */
    private enum Endpoint {
        POST_NEW_ALBUM(Pattern.compile("/albums")), GET_ALBUM_BY_KEY(Pattern.compile("^/[^/]+$"));

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }
}
