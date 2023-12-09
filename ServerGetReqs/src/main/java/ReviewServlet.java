import Controller.AlbumController;
import Service.MySQLService;
import Service.RedisService;
import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

@WebServlet(name = "ReviewServlet", value = "/review/*")
public class ReviewServlet extends HttpServlet {
    private MySQLService mySQLService;
    private HikariDataSource connectionPool;
    private AlbumController albumController;

    private RedisService redisService;
    private JedisPool redisConnectionPool;

    @Override
    public void init() {
        this.mySQLService = new MySQLService(3, 5);
        this.connectionPool = this.mySQLService.getConnectionPool();
        this.albumController = new AlbumController();

        this.redisService = new RedisService();
        this.redisConnectionPool = this.redisService.getConnectionPool();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        // Check we have url
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Missing parameters");
            return;
        }

        String albumId = urlPath.split("/")[1];

        try (Jedis connection = redisConnectionPool.getResource()) {
            Pipeline pipeline = connection.pipelined();
            Response<String> likesResponse = pipeline.hget(albumId, "NumberOfLikes");
            Response<String> dislikesResponse = pipeline.hget(albumId, "NumberOfDislikes");
            pipeline.sync();

            String numberOfLikes = likesResponse.get();
            String numberOfDislikes = dislikesResponse.get();

            if (numberOfLikes == null || numberOfDislikes == null) {
                throw new Exception("Redis query failed");
            }

            String messageToSend = "{\"likes\":\"" + numberOfLikes + "\",\"dislikes\":\"" + numberOfDislikes + "\"}";

            res.setStatus(HttpServletResponse.SC_OK);
            res.getWriter().write(messageToSend);
        } catch (Exception e) {
            try (Connection connection = this.connectionPool.getConnection()) {
                ResultSet resultSet = this.albumController.getAlbumProfile(connection, albumId);

                if (resultSet.next()) {
                    res.setStatus(HttpServletResponse.SC_OK);

                    String numberOfLikes = resultSet.getString("NumberOfLikes");
                    String numberOfDislikes = resultSet.getString("NumberOfDislikes");
                    String messageToSend = "{\"likes\":\"" + numberOfLikes + "\",\"dislikes\":\"" + numberOfDislikes + "\"}";

                    res.getWriter().write(messageToSend);
                } else {
                    // Album id does not exist in DB
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    res.getWriter().write("Key or Album not found");
                }
            } catch (SQLException ee) {
                res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                res.getWriter().write("There was an error with the database");
            }
        }
    }

    @Override
    public void destroy() {
        this.mySQLService.close();
        this.redisService.close();
    }
}
