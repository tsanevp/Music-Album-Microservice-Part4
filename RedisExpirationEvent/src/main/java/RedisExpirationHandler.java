import Controller.AlbumController;
import Service.MySQLService;
import com.zaxxer.hikari.HikariDataSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisExpirationHandler {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private static final String REDIS_CHANNEL = "__keyevent@0__:expired";

    private static HikariDataSource connectionPool;
    private static AlbumController albumController;

    public static void main(String[] args) {
        // Start a thread to handle Redis expiration events
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(RedisExpirationHandler::subscribeToRedisExpiryEvents);

        MySQLService service = new MySQLService();
        connectionPool = service.getConnectionPool();
        albumController = new AlbumController();

    }

    private static void subscribeToRedisExpiryEvents() {
        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT)) {
            jedis.psubscribe(new KeyExpiredListener(), REDIS_CHANNEL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class KeyExpiredListener extends JedisPubSub {
        @Override
        public void onPMessage(String pattern, String channel, String message) {
            // Handle the expired key
            handleExpiredKey(message);
            System.out.println(message);
        }
    }

    private static void handleExpiredKey(String key) {
        System.out.println(key);


//        try (Jedis jedis = new Jedis(REDIS_HOST, REDIS_PORT);) {
//            try (Connection sqlConnection = connectionPool.getConnection()) {
////                int rowsAffected = albumController.postToDatabase(sqlConnection, uuid, imageData, albumProfile);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
