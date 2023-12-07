package Util;

public class Constants {

    // RabbitMQ Constants
    public final static String RABBITMQ_HOST = "";
    public final static String EXCHANGE_NAME = "REVIEW_EXCHANGE";
    public final static String EXCHANGE_TYPE = "direct";
    public final static Integer CHANNEL_POOL_SIZE = 120;

    // MySQL Constants
    public static final String MYSQL_DB_URL = "";
    public static final String MYSQL_DB_USER = "";
    public static final String MYSQL_DB_PASSWORD = "";
    public final static int MIN_MYSQL_CONNECTIONS = 100;
    public final static int MAX_MYSQL_CONNECTIONS = 200;

    // Redis Constants
    public static final String REDIS_HOST = "";
    public static final int REDIS_PORT = 6379;
    public final static int MIN_REDIS_CONNECTIONS = 50;
    public final static int MAX_REDIS_CONNECTIONS = 75;
    public final static int MAX_REDIS_WAIT = 2500;
    public final static int REDIS_EXPIRE_TIME = 600;

}
