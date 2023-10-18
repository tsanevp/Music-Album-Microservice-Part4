import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String DB_URL = "jdbc:mysql://url/a2";
    private static final String DB_USER = "";
    private static final String DB_PASSWORD = "";
    private final Connection connection;
    public DatabaseConnector() {
        this.connection = connect();
    }

    public Connection getConnection() {
        return this.connection;
    }
    private Connection connect() {
        Connection connection = null;

        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Create a connection to the database
            connection = DriverManager.getConnection(DB_URL, DB_USER,  DB_PASSWORD);

            if (connection != null) {
                System.out.println("Connected to the database");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database");
            e.printStackTrace();
        }

        return connection;
    }

    protected void close() {
        if (this.connection != null) {
            try {
                this.connection.close();
                System.out.println("Connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
