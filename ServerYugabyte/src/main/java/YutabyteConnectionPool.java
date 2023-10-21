import com.yugabyte.ysql.YBClusterAwareDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class YutabyteConnectionPool {

    private static HikariDataSource connectionPool;

    public YutabyteConnectionPool() {
//        // Configure the pool with specific settings
        Properties poolProperties = new Properties();
        poolProperties.setProperty("dataSourceClassName", "com.yugabyte.ysql.YBClusterAwareDataSource");
        poolProperties.setProperty("maximumPoolSize", "10");
        poolProperties.setProperty("dataSource.serverName", "us-west-2.7279e1ed-c86a-42e1-a784-b820ad26c86f.aws.ybdb.io");
        poolProperties.setProperty("dataSource.portNumber", "5433");
        poolProperties.setProperty("dataSource.databaseName", "yugabyte");
        poolProperties.setProperty("dataSource.user", "admin");
        poolProperties.setProperty("dataSource.password", "Berabatov123!");
        poolProperties.setProperty("dataSource.ssl", "true"); // Enable SSL
        poolProperties.setProperty("dataSource.sslmode", "verify-full"); // Set SSL mode as required (e.g., verify-full, require, etc.)
        poolProperties.setProperty("dataSource.sslrootcert", "C:\\Users\\Peter\\Northeastern\\CS6650\\CS6650-Assignment2\\ServerYugabyte\\src\\main\\java\\root.crt"); // Path to the CA certificate file

//        HikariConfig config = new HikariConfig();
//        config.setDriverClassName("com.yugabyte.ysql.YBDriver");
//        config.setJdbcUrl("jdbc:yugabytedb://us-west-2.7279e1ed-c86a-42e1-a784-b820ad26c86f.aws.ybdb.io:5433/yugabyte?ssl=true&sslmode=verify-full&sslrootcert=src/main/java/root.crt");
//        config.setUsername("admin");
//        config.setPassword("Berabatov123!");
//        config.setMaximumPoolSize(10);

        poolProperties.setProperty("poolName", "connectionPool");
        HikariConfig config = new HikariConfig(poolProperties);
        config.validate();
//        HikariDataSource ds = new HikariDataSource(config);

        connectionPool = new HikariDataSource(config);
    }

    public HikariDataSource getConnectionPool() {
        return connectionPool;
    }

    public Connection getResource() throws SQLException {
        return connectionPool.getConnection();
    }
}
