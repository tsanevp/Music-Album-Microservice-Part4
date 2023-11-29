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
    private final Connection connection;
    private final String exchangeName;
    private final String exchangeType;
    private final String queueName;

    public ReviewRunnable(Connection connection, String exchangeName, String exchangeType, String queueName) {
        this.connection = connection;
        this.exchangeName = exchangeName;
        this.exchangeType = exchangeType;
        this.queueName = queueName;
    }

    @Override
    public void run() {
        try {
            Channel channel = this.connection.createChannel();

            channel.exchangeDeclare(this.exchangeName, this.exchangeType);
            channel.queueDeclare(this.queueName, false, false, false, null);
            channel.queueBind(this.queueName, this.exchangeName, this.queueName);

            System.out.println(" [*] Thread waiting for messages. To exit press CTRL+C");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                handleMessage(delivery, channel);
            };

            // Process messages
            boolean autoAck = true;
            channel.basicConsume(this.queueName, autoAck, deliverCallback, consumerTag -> {
            });
        } catch (Exception ex) {
            Logger.getLogger(ReviewConsumer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void handleMessage(Delivery delivery, Channel channel) throws IOException {
        try {
            // Use Gson to deserialize the JSON string to a Java object
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            JsonObject reviewJsonBody = gson.fromJson(message, JsonObject.class);

            // Get the albumId and review type
            String albumId = reviewJsonBody.get("albumId").getAsString();
            String reviewType = reviewJsonBody.get("reviewType").getAsString();

            // Increment review type value in DB
            try (java.sql.Connection connection = ReviewConsumer.connectionPool.getConnection()) {
                int rowsAffected = Objects.equals(reviewType, "like") ? ReviewConsumer.reviewController.addLike(connection, albumId) : ReviewConsumer.reviewController.addDislike(connection, albumId);

            } catch (SQLException e) {
                e.printStackTrace();
//                channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
            }

//            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
//            channel.basicReject(delivery.getEnvelope().getDeliveryTag(), true);
        }
    }
}

