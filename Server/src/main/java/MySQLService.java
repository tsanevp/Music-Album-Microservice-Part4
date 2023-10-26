import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLService {
    //        private static final String DB_URL = "jdbc:mysql://db1.cklnkwnnivsg.us-west-2.rds.amazonaws.com:3306/a2db1";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/a2";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "adminadmin";
    private final HikariDataSource connectionPool;

    /**
     * Constructor creating a new connection pool for the mySQL database.
     */
    public MySQLService() {
        this.connectionPool = this.connect();
    }

    /**
     * Returns the pool of connections to the mySQL database.
     *
     * @return - The pool of connections to the mySQL database.
     */
    public HikariDataSource getConnectionPool() {
        return this.connectionPool;
    }

    /**
     * Method to create the pool of connections to the mySQL database.
     *
     * @return - The pool of connections to the mySQL database.
     */
    private HikariDataSource connect() {
        HikariDataSource connectionPool = null;

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            config.setMinimumIdle(10);
            config.setMaximumPoolSize(145);

            connectionPool = new HikariDataSource(config);
            System.out.println("connected to db");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found");
            e.printStackTrace();
        }

        return connectionPool;
    }

    /**
     * Closes the connection pool to the mySQL database.
     */
    protected void close() {
        if (this.connectionPool != null) {
            this.connectionPool.close();
            System.out.println("Connection closed");
        }
    }
}
