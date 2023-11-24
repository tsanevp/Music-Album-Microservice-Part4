import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReviewRunnable implements Runnable {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final static String QUEUE_NAME = "QUEUE_REVIEWS";
    private final static String EXCHANGE_NAME = "EXCHANGE_TEST";
    private final Connection connection;
    private final String queueName;

    public ReviewRunnable(Connection connection, String queueName) {
        this.connection = connection;
        this.queueName = queueName;
    }

    @Override
    public void run() {
        try {
            Channel channel = connection.createChannel();

            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            channel.queueDeclare(queueName, false, false, false, null);
            channel.queueBind(queueName, EXCHANGE_NAME, queueName);

            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                handleMessage(delivery, channel);
            };

            // process messages
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {
            });
        } catch (Exception ex) {
            Logger.getLogger(ReviewConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleMessage(Delivery delivery, Channel channel) throws IOException {
        try {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            // Use Gson to deserialize the JSON string to a Java object
            JsonObject reviewJsonBody = gson.fromJson(message, JsonObject.class);

            // Get the albumId and review type
            String albumId = reviewJsonBody.get("albumId").getAsString();
            String reviewType = reviewJsonBody.get("reviewType").getAsString();

            // Increment review type value in DB
            try (java.sql.Connection connection = ReviewConsumer.connectionPool.getConnection()) {
                int rowsAffected = Objects.equals(reviewType, "like") ? ReviewConsumer.reviewController.addLike(connection, albumId) : ReviewConsumer.reviewController.addDislike(connection, albumId);

            } catch (SQLException e) {
                channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
            }

            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
        }
    }
}

