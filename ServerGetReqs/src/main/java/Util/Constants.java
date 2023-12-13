package Util;

public class Constants {

    // MySQL Constants
    public static final String MYSQL_DB_URL = "jdbc:mysql://db1.cklnkwnnivsg.us-west-2.rds.amazonaws.com:3306/a3db1";
    public static final String MYSQL_DB_USER = "admin";
    public static final String MYSQL_DB_PASSWORD = "adminpassword";

    // Redis Constants
    public static final String REDIS_HOST = "redis-cluster-1.hs6qqe.clustercfg.usw2.cache.amazonaws.com";
    public static final int REDIS_PORT = 6379;
    public final static int MIN_REDIS_CONNECTIONS = 5;
    public final static int MAX_REDIS_CONNECTIONS = 10;
    public final static int MAX_REDIS_WAIT = 2500;
}