import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Pattern;

@WebServlet(name = "AlbumsServlet", value = "/albums")
public class AlbumsServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("text/plain");
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

        if (!albumId.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_OK);
            JSONObject jsonObject = new JSONObject().put("artist", "Sex Pistols").put("title", "Never Mind The Bollocks").put("year", "1997");
            res.getWriter().write(jsonObject.toString());
        } else {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Key not found");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");

        String urlPath = req.getPathInfo();
        String servletPath = req.getServletPath();

        // Check we have an empty url, the servlet path was called and that POST is multipart form
        if (urlPath != null || !isUrlValid(servletPath) || !ServletFileUpload.isMultipartContent(req)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("invalid request");
            return;
        }

        JSONObject jsonObject = new JSONObject();

        try {
            ServletFileUpload upload = new ServletFileUpload();
            FileItemIterator iterator = upload.getItemIterator(req);

            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();

                if (item.isFormField()) {
                    System.out.println("This is an album property: " + item.getFieldName());
                    continue;
                }

                try (InputStream inputStream = item.openStream()) {
                    int imageSize = inputStream.readAllBytes().length;
                    
                    jsonObject.put("albumID", "id");
                    jsonObject.put("imageSize", String.valueOf(imageSize));
                    res.setStatus(HttpServletResponse.SC_OK);
                    res.getWriter().write(jsonObject.toString());
                } catch (IOException e) {
                    System.out.println("error opening file steam");
                }
            }
        } catch (FileUploadException e) {
            System.out.println("error parsing multipart/form data");
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to return whether the path provided is an expected endpoint.
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
        GET_ALBUM_BY_KEY(Pattern.compile("^/\\d+$"));

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }
}
