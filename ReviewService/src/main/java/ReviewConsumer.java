import Controller.ReviewController;
import Service.MySQLService;
import Service.RabbitMQService;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {
    private final static Integer MAX_THREADS = 2000;
    private final static MySQLService mySQLService = new MySQLService();
    protected static HikariDataSource connectionPool;
    protected static ReviewController reviewController;
    protected static RabbitMQService rabbitMQService;

    public static void main(String[] argv) throws Exception {
        connectionPool = mySQLService.getConnectionPool();
        reviewController = new ReviewController();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        ExecutorService servicePool = Executors.newFixedThreadPool(MAX_THREADS);
        for (int i = 0; i < MAX_THREADS; i++) {
            servicePool.execute(new ReviewRunnable(connection));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            connectionPool.close();
        }));
    }
}
