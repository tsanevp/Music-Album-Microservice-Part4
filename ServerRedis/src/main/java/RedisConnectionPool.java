import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

public class RedisConnectionPool {

    private static JedisPool jedisPool;

    public RedisConnectionPool() {
        // Configure the pool with specific settings
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(500); // Maximum total connections
        poolConfig.setMaxIdle(5);  // Maximum idle connections
        poolConfig.setMinIdle(1);  // Minimum idle connections

        // Create the JedisPool with host and port
        jedisPool = new JedisPool(poolConfig, Protocol.DEFAULT_HOST, 6379);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public Jedis getResource() {
        return jedisPool.getResource();
    }
}
