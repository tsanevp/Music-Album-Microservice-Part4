package Util;

public class Constants {

    // S3 Bucket Name
    public final static String BUCKET_NAME = "cs6650-imagebucket";

    // RabbitMQ Constants
    public final static String RABBITMQ_HOST = "ec2-18-236-188-67.us-west-2.compute.amazonaws.com";
    public final static String EXCHANGE_NAME = "REVIEW_EXCHANGE";
    public final static String EXCHANGE_TYPE = "direct";
    public final static Integer CHANNEL_POOL_SIZE = 200;

    // MySQL Constants
    public static final String MYSQL_DB_URL = "jdbc:mysql://db1.cklnkwnnivsg.us-west-2.rds.amazonaws.com:3306/a3db1";
    public static final String MYSQL_DB_USER = "admin";
    public static final String MYSQL_DB_PASSWORD = "adminpassword";
    public final static int MIN_MYSQL_CONNECTIONS = 100;
    public final static int MAX_MYSQL_CONNECTIONS = 190;

    // Redis Constants
    public static final String REDIS_HOST = "redis-cluster-1.hs6qqe.clustercfg.usw2.cache.amazonaws.com";
    public static final int REDIS_PORT = 6379;
    public final static int MIN_REDIS_CONNECTIONS = 100;
    public final static int MAX_REDIS_CONNECTIONS = 200;
    public final static int MAX_REDIS_WAIT = 2500;
    public final static int REDIS_EXPIRE_TIME = 60;
}
