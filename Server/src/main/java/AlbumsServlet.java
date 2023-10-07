import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.model.ImageMetaData;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 10,    // 10 MB
        maxFileSize = 1024 * 1024 * 50,        // 50 MB
        maxRequestSize = 1024 * 1024 * 100)    // 100 MB
public class AlbumsServlet extends HttpServlet {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
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

        // Check collection to get album from album id
        if (albumId.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Key not found");
            return;
        }

        res.setStatus(HttpServletResponse.SC_OK);
        String json = gson.toJson(new AlbumInfo().artist("Sex Pistols").title("Never Mind The Bollocks").year("1997"));
        res.getWriter().write(json);
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

        // Check we have a valid image part
        Part image = req.getPart("image");
        if (image == null || !isImageContentType(image.getContentType())) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid or missing image part");
            return;
        }

//        String title = req.getPart("title").toString();
//        String year = req.getPart("year").toString();
//        String artist = req.getPart("artist").toString();

        // Get information from image part
        long imageSize = image.getSize();
        res.setStatus(HttpServletResponse.SC_OK);
        String json = gson.toJson(new ImageMetaData().albumID("id").imageSize(String.valueOf(imageSize)));
        res.getWriter().write(json);
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
        GET_ALBUM_BY_KEY(Pattern.compile("^/\\d+$")); // Atm expects an int ID, will change in later assignments

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }
}
