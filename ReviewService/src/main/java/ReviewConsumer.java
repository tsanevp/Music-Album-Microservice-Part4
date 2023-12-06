import Controller.ReviewController;
import Service.MySQLService;
import Service.RedisService;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {
    private final static Integer NUM_CONSUMERS_EACH_QUEUE = 100;
    protected static ReviewController reviewController = new ReviewController();
    private final static MySQLService mySQLService = new MySQLService();
    protected static HikariDataSource connectionPool;

    private final static RedisService redisService = new RedisService();
    protected static JedisPool redisConnectionPool;

    private final static String HOST = "";
    private final static String EXCHANGE_NAME = "REVIEW_EXCHANGE";
    private final static String EXCHANGE_TYPE = "direct";
    private final static String LIKE_QUEUE = "like";
    private final static String DISLIKE_QUEUE = "dislike";


    public static void main(String[] argv) throws Exception {
        connectionPool = mySQLService.getConnectionPool();
        redisConnectionPool = redisService.getConnectionPool();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);

        Connection connectionLikes = factory.newConnection();
        Connection connectionDislikes = factory.newConnection();

        ExecutorService servicePoolLikes = Executors.newFixedThreadPool(NUM_CONSUMERS_EACH_QUEUE);
        for (int i = 0; i < NUM_CONSUMERS_EACH_QUEUE; i++) {
            servicePoolLikes.execute(new ReviewRunnable(connectionLikes, EXCHANGE_NAME, EXCHANGE_TYPE, LIKE_QUEUE));
        }

        ExecutorService servicePoolDislikes = Executors.newFixedThreadPool(NUM_CONSUMERS_EACH_QUEUE);
        for (int i = 0; i < NUM_CONSUMERS_EACH_QUEUE; i++) {
            servicePoolDislikes.execute(new ReviewRunnable(connectionDislikes, EXCHANGE_NAME, EXCHANGE_TYPE, DISLIKE_QUEUE));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(mySQLService::close));
    }
}

