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
     * Constructor creating a new channel pool for RabbitMQ.
     */
    public RabbitMQService() {
        this.connectionPool = this.connect();
    }

    /**
     * Returns the pool of channels to RabbitMQ.
     *
     * @return - The pool of channels to RabbitMQ.
     */
    public GenericObjectPool<Channel> getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Method to create the pool of channels to RabbitMQ.
     *
     * @return - The pool of channels to RabbitMQ.
     */
    private GenericObjectPool<Channel> connect() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ec2-54-212-246-9.us-west-2.compute.amazonaws.com");

        try {
            this.connection = factory.newConnection();
            GenericObjectPoolConfig<Channel> config = new GenericObjectPoolConfig<>();
            config.setMinIdle(1);
            config.setMaxIdle(3);
            config.setMaxTotal(10);

            RabbitMQChannelFactory rabbitMQChannelFactory = new RabbitMQChannelFactory(this.connection);
            return new GenericObjectPool<>(rabbitMQChannelFactory, config);
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the channel pool, then closes the connection.
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
