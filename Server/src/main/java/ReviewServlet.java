import Service.RabbitMQService;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;


@WebServlet(name = "ReviewServlet", value = "/review/*")
public class ReviewServlet extends HttpServlet {
    private final static String EXCHANGE_NAME = "EXCHANGE_REVIEWS";
//    private RabbitMQService rabbitMQService;
//    private GenericObjectPool<Channel> connectionPool;
    private ConcurrentLinkedDeque<Channel> connectionPool = new ConcurrentLinkedDeque<>();


    public ReviewServlet() {
    }

    @Override
    public void init() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connection = factory.newConnection();

            for (int i = 0; i < 200; i++) {

                connectionPool.add(connection.createChannel());
            }

        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
//        this.rabbitMQService = new RabbitMQService();
//        this.connectionPool = this.rabbitMQService.getConnectionPool();

    }

        @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        res.setContentType("application/json");
        String urlPath = req.getPathInfo();

        // Check we have url
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("Missing parameters");
            return;
        }

        String[] urlParts = urlPath.split("/");

        // Check we have a valid uri arguments
        if (!isUrlValid(urlParts)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.getWriter().write("Invalid or missing inputs");
            return;
        }

        String reviewType = urlParts[1];
        String albumId = urlParts[2];

        HashMap<String, String> messageData = new HashMap<>();
        messageData.put("albumId", albumId);
        messageData.put("reviewType", reviewType);
        String messageToSend = new Gson().toJson(messageData);

        Channel channel = null;
        try {
            channel = this.connectionPool.removeFirst();
            channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            channel.basicPublish(EXCHANGE_NAME, "", null, messageToSend.getBytes(StandardCharsets.UTF_8));

            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write("Review sent");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                this.connectionPool.add(channel);
            }
        }
    }

    /**
     * Method to return whether the parts provided are uri arguments for the like or dislike review.
     *
     * @param urlParts - The current uri arguments being evaluated.
     * @return true if the url is a valid endpoint, false otherwise.
     */
    private boolean isUrlValid(String[] urlParts) {
        for (Endpoint endpoint : Endpoint.values()) {
            Pattern pattern = endpoint.pattern;

            // Only check if "like" or "dislike" appears. UUID can be anything.
            if (pattern.matcher(urlParts[1]).matches() && urlParts.length == 3) {
                return true;
            }
        }

        return false;
    }

    /**
     * Enum constants that represent different review endpoints
     */
    private enum Endpoint {
        LIKE_REVIEW(Pattern.compile("like")), DISLIKE_REVIEW(Pattern.compile("dislike"));

        public final Pattern pattern;

        Endpoint(Pattern pattern) {
            this.pattern = pattern;
        }
    }

    @Override
    public void destroy() {
//        this.rabbitMQService.close();
    }
}
