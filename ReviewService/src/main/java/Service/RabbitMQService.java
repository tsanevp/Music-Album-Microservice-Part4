package Service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class RabbitMQService {
    private Connection connection;
    private final GenericObjectPool<Channel> connectionPool;

    /**
     * Constructor creating a new connection pool for the mySQL database.
     */
    public RabbitMQService() {
        this.connectionPool = this.connect();
    }

    /**
     * Returns the pool of connections to the mySQL database.
     *
     * @return - The pool of connections to the mySQL database.
     */
    public GenericObjectPool<Channel> getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Method to create the pool of connections to the mySQL database.
     *
     * @return - The pool of connections to the mySQL database.
     */
    private GenericObjectPool<Channel> connect() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            this.connection = factory.newConnection();
            GenericObjectPoolConfig<Channel> config = new GenericObjectPoolConfig<>();
            config.setMinIdle(2);
            config.setMaxIdle(5);
            config.setMaxTotal(150);

            RabbitMQChannelFactory rabbitMQChannelFactory = new RabbitMQChannelFactory(this.connection);
            return new GenericObjectPool<Channel>(rabbitMQChannelFactory, config);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the connection pool to the mySQL database.
     */
    public void close() {
        if (this.connectionPool != null) {
            this.connectionPool.close();
            System.out.println("Connection closed");
        }

        if (this.connection != null) {
            try {
                this.connection.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
