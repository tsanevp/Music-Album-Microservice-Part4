import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Pattern;

@WebServlet(name = "ReviewServlet", value = "/review/*")
public class ReviewServlet extends HttpServlet {
    private MySQLService mySQLService;
    private HikariDataSource connectionPool;
    private ReviewController reviewController;

    @Override
    public void init() {
        this.reviewController = new ReviewController();
        this.mySQLService = new MySQLService();
        this.connectionPool = this.mySQLService.getConnectionPool();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        // Check we have url
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        // Check we have a valid uri arguments
        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid or missing inputs");
            return;
        }

        String reviewType = urlParts[1];
        String albumId = urlParts[2];

        // Post image info and album profile to db
        try (Connection connection = this.connectionPool.getConnection()) {
            int rowsAffected = Objects.equals(reviewType, "like") ? this.reviewController.addLike(connection, albumId) : this.reviewController.addDislike(connection, albumId);

            if (rowsAffected > 0) {
                res.setStatus(HttpServletResponse.SC_OK);
                res.getWriter().write("review made");
            } else {
                res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                res.getWriter().write("Album not found");
            }
        } catch (SQLException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.getWriter().write("There was an error with the database");
        }
    }

    /**
     * Method to return whether the parts provided are uri arguments for the like or dislike review.
     *
     * @param urlParts - The current uri arguments being evaluated.
     * @return true if the url is a valid endpoint, false otherwise.
     */
    private boolean isUrlValid(String[] urlParts) {
        for (Endpoint endpoint : Endpoint.values()) {
            Pattern pattern = endpoint.pattern;

            // Only check if "like" or "dislike" appears. UUID can be anything.
            if (pattern.matcher(urlParts[1]).matches() && urlParts.length == 3) {
                return true;
            }
        }

        return false;
    }

    /**
     * Enum constants that represent different possible endpoints
     */
    private enum Endpoint {
        LIKE_REVIEW(Pattern.compile("like")), DISLIKE_REVIEW(Pattern.compile("dislike"));

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }

    @Override
    public void destroy() {
        mySQLService.close();
    }
}
