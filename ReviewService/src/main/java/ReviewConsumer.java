import Controller.ReviewController;
import Service.MySQLService;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {
    private final static Integer MAX_THREADS = 120;
    private final static MySQLService mySQLService = new MySQLService();
    protected static HikariDataSource connectionPool;
    protected static ReviewController reviewController;

    public static void main(String[] argv) throws Exception {
        connectionPool = mySQLService.getConnectionPool();
        reviewController = new ReviewController();

        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("ec2-35-166-228-31.us-west-2.compute.amazonaws.com");
        factory.setHost("localhost");

        Connection connectionLikes = factory.newConnection();
        Connection connectionDislikes = factory.newConnection();


        ExecutorService servicePoolLikes = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i = 0; i < MAX_THREADS; i++) {
            servicePoolLikes.execute(new ReviewRunnable(connectionLikes, "like"));
        }

        ExecutorService servicePoolDislikes = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i = 0; i < MAX_THREADS; i++) {
            servicePoolDislikes.execute(new ReviewRunnable(connectionDislikes, "dislike"));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            connectionPool.close();
        }));
    }
}

