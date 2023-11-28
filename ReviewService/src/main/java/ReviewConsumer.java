import Controller.ReviewController;
import Service.MySQLService;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {
    private final static Integer MAX_THREADS = 100;
    private final static MySQLService mySQLService = new MySQLService();
    protected static HikariDataSource connectionPool;
    protected static ReviewController reviewController;

    private final static String HOST = "localhost";
    private final static String EXCHANGE_NAME = "EXCHANGE_TEST";
    private final static String EXCHANGE_TYPE = "direct";
    private final static String LIKE_QUEUE = "like";
    private final static String DISLIKE_QUEUE = "dislike";


    public static void main(String[] argv) throws Exception {
        connectionPool = mySQLService.getConnectionPool();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(HOST);

        Connection connectionLikes = factory.newConnection();
        Connection connectionDislikes = factory.newConnection();

        ExecutorService servicePoolLikes = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i = 0; i < MAX_THREADS; i++) {
            servicePoolLikes.execute(new ReviewRunnable(connectionLikes, EXCHANGE_NAME, EXCHANGE_TYPE, LIKE_QUEUE));
        }

        ExecutorService servicePoolDislikes = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i = 0; i < MAX_THREADS; i++) {
            servicePoolDislikes.execute(new ReviewRunnable(connectionDislikes, EXCHANGE_NAME, EXCHANGE_TYPE, DISLIKE_QUEUE));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(mySQLService::close));
    }
}

