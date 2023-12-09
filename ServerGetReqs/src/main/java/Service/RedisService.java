package Service;

import Util.Constants;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisService {
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
        JedisPool connectionPool = new JedisPool(config, Constants.REDIS_HOST, Constants.REDIS_PORT);
        System.out.println("connected to redis db");

        return connectionPool;
    }

    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(Constants.MIN_REDIS_CONNECTIONS);
        config.setMaxTotal(Constants.MAX_REDIS_CONNECTIONS);
        config.setMaxWait(Duration.ofMillis(Constants.MAX_REDIS_WAIT));
        config.setBlockWhenExhausted(true);
        config.setTestOnBorrow(true);

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