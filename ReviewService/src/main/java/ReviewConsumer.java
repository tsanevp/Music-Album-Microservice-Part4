import Controller.ReviewController;
import Service.MySQLService;
import Util.Constants;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zaxxer.hikari.HikariDataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {
    private final static MySQLService mySQLService = new MySQLService();
    protected static ReviewController reviewController = new ReviewController();
    protected static HikariDataSource connectionPool;

    public static void main(String[] argv) throws Exception {
        connectionPool = mySQLService.getConnectionPool();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Constants.HOST);

        Connection connectionLikes = factory.newConnection();
        Connection connectionDislikes = factory.newConnection();

        ExecutorService servicePoolLikes = Executors.newFixedThreadPool(Constants.NUM_CONSUMERS_EACH_QUEUE);
        for (int i = 0; i < Constants.NUM_CONSUMERS_EACH_QUEUE; i++) {
            servicePoolLikes.execute(new ReviewRunnable(connectionLikes, Constants.EXCHANGE_NAME, Constants.EXCHANGE_TYPE, Constants.LIKE_QUEUE));
        }

        ExecutorService servicePoolDislikes = Executors.newFixedThreadPool(Constants.NUM_CONSUMERS_EACH_QUEUE);
        for (int i = 0; i < Constants.NUM_CONSUMERS_EACH_QUEUE; i++) {
            servicePoolDislikes.execute(new ReviewRunnable(connectionDislikes, Constants.EXCHANGE_NAME, Constants.EXCHANGE_TYPE, Constants.DISLIKE_QUEUE));
        }

        Runtime.getRuntime().addShutdownHook(new Thread(mySQLService::close));
    }
}

