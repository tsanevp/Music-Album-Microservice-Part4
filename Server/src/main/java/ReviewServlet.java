import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

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
    private final static String EXCHANGE_NAME = "EXCHANGE_TEST";
    private final ConcurrentLinkedDeque<Channel> connectionPoolLikes = new ConcurrentLinkedDeque<>();
    private final ConcurrentLinkedDeque<Channel> connectionPoolDislikes = new ConcurrentLinkedDeque<>();

    public ReviewServlet() {
    }

    @Override
    public void init() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        try {
            Connection connectionLikes = factory.newConnection();
            Connection connectionDislikes = factory.newConnection();

            for (int i = 0; i < 120; i++) {
                Channel channel = connectionLikes.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, "direct");
                connectionPoolLikes.add(channel);

                Channel channel2 = connectionDislikes.createChannel();
                channel2.exchangeDeclare(EXCHANGE_NAME, "direct");
                connectionPoolDislikes.add(channel2);
            }

        } catch (IOException | TimeoutException ex) {
            throw new RuntimeException(ex);
        }
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
            if (reviewType.equals("like")) {
                channel = this.connectionPoolLikes.removeFirst();
                channel.basicPublish(EXCHANGE_NAME, "like", null, messageToSend.getBytes(StandardCharsets.UTF_8));
            } else {
                channel = this.connectionPoolDislikes.removeFirst();
                channel.basicPublish(EXCHANGE_NAME, "dislike", null, messageToSend.getBytes(StandardCharsets.UTF_8));
            }

            res.setStatus(HttpServletResponse.SC_CREATED);
            res.getWriter().write("Review sent");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                if (reviewType.equals("like")) {
                    this.connectionPoolLikes.add(channel);
                } else {
                    this.connectionPoolDislikes.add(channel);
                }
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

    private void closeChannels(ConcurrentLinkedDeque<Channel> connectionPool) {
        for (Channel channel : connectionPool) {
            try {
                channel.close();
            } catch (IOException | TimeoutException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void destroy() {
        closeChannels(this.connectionPoolLikes);
        closeChannels(this.connectionPoolDislikes);
    }
}
