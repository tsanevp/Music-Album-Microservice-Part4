import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;

@WebListener
public class MyServletContextListener implements ServletContextListener {
    DatabaseConnector databaseConnector;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        // Initialization code (e.g., database connection setup)
        // This code will run when your application is deployed.
        databaseConnector = new DatabaseConnector();
        Connection connection = databaseConnector.getConnection();

        event.getServletContext().setAttribute("connection", connection);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // Cleanup code (e.g., closing database connections)
        // This code will run when your application is undeployed.
        databaseConnector.close();
    }
}