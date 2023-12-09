package Service;

import Util.Constants;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;

public class RabbitMQService {

    public RabbitMQService() {}

    /**
     * Method to create the pool of channels to RabbitMQ.
     *
     * @return - The pool of channels to RabbitMQ.
     */
    public ConcurrentLinkedDeque<Channel> createChannelPool(String host, String exchangeName, String exchangeType) {
        ConcurrentLinkedDeque<Channel> channelPool = new ConcurrentLinkedDeque<>();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);

        try {
            Connection connection = factory.newConnection();

            for (int i = 0; i < Constants.CHANNEL_POOL_SIZE; i++) {
                Channel channel = connection.createChannel();
                channel.exchangeDeclare(exchangeName, exchangeType);
                channelPool.add(channel);
            }

        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }

        return channelPool;
    }

    /**
     * Closes the channel pool to RabbitMQ.
     */
    public void closeChannelPool(ConcurrentLinkedDeque<Channel> channelPool) {
        for (Channel channel : channelPool) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
