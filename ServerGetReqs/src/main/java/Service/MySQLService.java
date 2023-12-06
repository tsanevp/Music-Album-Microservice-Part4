package Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class MySQLService {
    private static final String DB_URL = "";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
    private final int minConnections;
    private final int maxConnections;
    private final HikariDataSource connectionPool;

    /**
     * Constructor creating a new connection pool for the mySQL database.
     */
    public MySQLService(int minConnections, int maxConnections) {
        this.minConnections = minConnections;
        this.maxConnections = maxConnections;
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
            config.setMinimumIdle(this.minConnections);
            config.setMaximumPoolSize(this.maxConnections);

            connectionPool = new HikariDataSource(config);
            System.out.println("connected to mysql db");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found");
            e.printStackTrace();
        }

        return connectionPool;
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
