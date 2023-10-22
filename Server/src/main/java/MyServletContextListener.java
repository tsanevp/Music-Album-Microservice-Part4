import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;

@WebListener
public class MyServletContextListener implements ServletContextListener {
    DatabaseConnector databaseConnector;
    HikariDataSource dataSource;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Initialization code (e.g., database connection setup)
        // This code will run when your application is deployed.
//        databaseConnector = new DatabaseConnector();
//        Connection connection = databaseConnector.getConnection();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mysql://localhost:3306/a2");
            config.setUsername("root");
            config.setPassword("adminadmin");

//            config.setJdbcUrl("jdbc:mysql://db1.cklnkwnnivsg.us-west-2.rds.amazonaws.com:3306/a2db1");
//            config.setUsername("admin");
//            config.setPassword("adminpassword");
            config.setMaximumPoolSize(300);

            dataSource = new HikariDataSource(config);

            event.getServletContext().setAttribute("connectionPool", dataSource);

        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }



//        event.getServletContext().setAttribute("connection", connection);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // Cleanup code (e.g., closing database connections)
        // This code will run when your application is undeployed.
//        databaseConnector.close();

        if (dataSource != null) {
            dataSource.close();
        }
    }
}