package Util;

public class Constants {

    // RabbitMQ Constants
    public final static String HOST = "";
    public final static String EXCHANGE_NAME = "REVIEW_EXCHANGE";
    public final static String EXCHANGE_TYPE = "direct";
    public final static Integer NUM_CONSUMERS_EACH_QUEUE = 100;
    public final static String LIKE_QUEUE = "like";
    public final static String DISLIKE_QUEUE = "dislike";

    // MySQL Constants
    public static final String DB_URL = "";
    public static final String DB_USER = "";
    public static final String DB_PASSWORD = "";
    public final static int MIN_NUM_CONNECTIONS = 50;
    public final static int MAX_NUM_CONNECTIONS = 75;

    // Redis Constants
    public static final String REDIS_HOST = "";
    public static final int REDIS_PORT = 6379;
    public final static int MIN_REDIS_CONNECTIONS = 50;
    public final static int MAX_REDIS_CONNECTIONS = 75;
    public final static int MAX_REDIS_WAIT = 2500;
}