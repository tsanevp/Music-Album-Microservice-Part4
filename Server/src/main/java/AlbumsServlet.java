import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zaxxer.hikari.HikariDataSource;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;

import java.io.InputStream;
import java.util.regex.Matcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums/*")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
public class AlbumsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Connection connection;

    HikariDataSource connectionPool;
    //    HikariDataSource connectionPool = (HikariDataSource) getServletContext().getAttribute("connectionPool");


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        // Check we have url
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("missing parameters");
            return;
        }

        // Validate url path and ensure we have a valid request
        if (!isUrlValid(urlPath)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("invalid request");
            return;
        }

        String albumId = urlPath.split("/")[1];

        connectionPool = (HikariDataSource) getServletContext().getAttribute("connectionPool");
        try (Connection connection = connectionPool.getConnection()) {
//        try {
            ResultSet resultSet = getAlbumProfile(connection, albumId);

            if (resultSet.next()) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write(resultSet.getString("AlbumProfile"));
            } else {
                // Album id does not exist in DB
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("Key not found");
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("There was an error with the database");
        }
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

        // Check we have a valid image part and album profile
        Part image = req.getPart("image");
        Part albumProfilePart = req.getPart("profile");

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
        long imageSize = image.getSize();

        // Create image data and profile json strings
        String imageData = gson.toJson(new ImageMetaData().albumID(uuid).imageSize(String.valueOf(imageSize)));
        String albumProfile = gson.toJson(new AlbumsProfile().artist(artist).title(title).year(year));

        // Post image info and album profile to db
        connectionPool = (HikariDataSource) getServletContext().getAttribute("connectionPool");

        try (Connection connection = connectionPool.getConnection()) {
//        try {
            int rowsAffected = postToDatabase(connection, uuid, imageData, albumProfile);

            if (rowsAffected > 0) {
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

    private int postToDatabase(Connection connection, String uuid, String imageData, String albumProfile) throws SQLException {
//        connection = (Connection) getServletContext().getAttribute("connection");

        String insertQuery = "INSERT INTO albumRequests (AlbumID, ImageData, AlbumProfile) VALUES (?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        // Set values for the prepared statement
        preparedStatement.setString(1, uuid);
        preparedStatement.setString(2, imageData);
        preparedStatement.setString(3, albumProfile);

        // Execute the insert statement
        return preparedStatement.executeUpdate();
    }

    private String[] parseAlbumProfile(Part albumProfilePart) throws IOException {
//        ObjectMapper objectMapper = new ObjectMapper();
//        try (InputStream inputStream = albumProfilePart.getInputStream()) {
//            System.out.println(inputStream);
//            AlbumsProfile albumsProfile = objectMapper.readValue(preprocessData(albumProfilePart.getInputStream().toString()), AlbumsProfile.class);
//            System.out.println(albumsProfile.getTitle());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

//        AlbumsProfile albumsProfile = gson.fromJson(albumProfilePart.getInputStream().toString().replaceAll("class AlbumsProfile ", ""), AlbumsProfile.class);
//        System.out.println(albumsProfile.getYear());


        String jsonContent = new String(albumProfilePart.getInputStream().readAllBytes());

//                AlbumsProfile albumsProfile = (AlbumsProfile) albumProfilePart.getInputStream().readAllBytes().toString();
        String[] lines = jsonContent.split("\n");
        String artist = null;
        String title = null;
        String year = null;

        for (String line : lines) {
            String[] parts = line.trim().split(":");
            if (parts.length == 2) {
                String key = parts[0].trim();
                String value = parts[1].trim();

                switch (key) {
                    case "artist" -> artist = value;
                    case "title" -> title = value;
                    case "year" -> year = value;
                }
            }
        }

        return new String[]{artist, title, year};
    }

    private ResultSet getAlbumProfile(Connection connection, String albumId) throws SQLException {
//        connection = (Connection) getServletContext().getAttribute("connection");

//        String selectQuery = "SELECT JSON_EXTRACT(AlbumProfile, '$') AS AlbumData FROM albumRequests WHERE AlbumID = ?";
        String selectQuery = "SELECT AlbumProfile FROM albumRequests WHERE AlbumID = ?";

        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

        // Set values for the prepared statement
        preparedStatement.setString(1, albumId);

        // Execute the insert statement
        return preparedStatement.executeQuery();
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

    /**
     * Enum constants that represent different possible endpoints
     */
    private enum Endpoint {
        POST_NEW_ALBUM(Pattern.compile("/albums")),
        GET_ALBUM_BY_KEY(Pattern.compile("^/[^/]+$"));

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }
//    public String preprocessData(String inputData) {
//        // Use a regular expression to match "class" and the class name
//        Pattern pattern = Pattern.compile("class\\s+(\\w+)\\s*\\{(.+?)}", Pattern.DOTALL);
//        Matcher matcher = pattern.matcher(inputData);
//
//        if (matcher.find()) {
//            String className = matcher.group(1);
//            // Extract the content within the curly braces
//            String content = matcher.group(2);
//
//            // Replace "key: value" with "key": "value" to make it valid JSON
//            content = content.replaceAll("(\\w+):\\s+([^\\n]+)", "\"$1\": \"$2\"");
//
//            // Wrap the content in curly braces to create valid JSON and include the class name
//            return "{\"" + className + "\": {" + content + "}}";
//        }
//
//        return null; // Invalid input format
//    }

}
