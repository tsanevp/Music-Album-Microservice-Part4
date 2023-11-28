package Service;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisService {
    private static final String REDIS_HOST = "localhost";
    private static final int REDIS_PORT = 6379;
    private final JedisPool connectionPool;

    /**
     * Constructor creating a new connection pool for the mySQL database.
     */
    public RedisService() {
        this.connectionPool = this.connect();
    }

    /**
     * Returns the pool of connections to the mySQL database.
     *
     * @return - The pool of connections to the mySQL database.
     */
    public JedisPool getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Method to create the pool of connections to the mySQL database.
     *
     * @return - The pool of connections to the mySQL database.
     */
    private JedisPool connect() {
        JedisPoolConfig config = getJedisPoolConfig();
        JedisPool connectionPool = new JedisPool(config, REDIS_HOST, REDIS_PORT);
        System.out.println("connected to db");

        return connectionPool;
    }

    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(5);
        config.setMaxIdle(50);
        config.setMaxTotal(200);
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(true);
        config.setMaxWait(Duration.ofMillis(2500));

        return config;
    }

    /**
     * Closes the connection pool to the mySQL database.
     */
    public void close() {
        if (this.connectionPool != null) {
            this.connectionPool.close();
            System.out.println("Connection closed");
        }
    }
}
