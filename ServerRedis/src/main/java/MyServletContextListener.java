import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

@WebListener
public class MyServletContextListener implements ServletContextListener {

    Jedis connection;
    JedisPool jedisPool;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            jedisPool = new RedisConnectionPool().getJedisPool();
            event.getServletContext().setAttribute("jedisPool", jedisPool);
        } catch (JedisConnectionException e) {
            System.out.println("Error connecting to db");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        jedisPool.close();
    }
}