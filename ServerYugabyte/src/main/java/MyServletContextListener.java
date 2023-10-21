import com.yugabyte.ysql.YBClusterAwareDataSource;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.SQLException;

@WebListener
public class MyServletContextListener implements ServletContextListener {
//    Connection connection;
    HikariDataSource connectionPool;

    @Override
    public void contextInitialized(ServletContextEvent event) {
//        try {
//            YBClusterAwareDataSource ds = new YBClusterAwareDataSource();
//            ds.setUrl("jdbc:yugabytedb://us-west-2.7279e1ed-c86a-42e1-a784-b820ad26c86f.aws.ybdb.io:5433/yugabyte");
//            ds.setUser("admin");
//            ds.setPassword("Berabatov123!");
//            ds.setSslcert("src/main/java/root.crt");
//            ds.setLoadBalanceHosts(true);
//
//            connection = ds.getConnection();
//
//            // You now have a database connection.
//            event.getServletContext().setAttribute("connection", connection);
//
//            System.out.println("Connection made");
//        } catch (SQLException e) {
//            e.printStackTrace();
//            // Handle connection errors here.
//        }
        try {
            connectionPool = new YutabyteConnectionPool().getConnectionPool();
            event.getServletContext().setAttribute("connectionPool", connectionPool);
        } catch (HikariPool.PoolInitializationException e) {
            System.out.println("Error connection to db and creating pool");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        connectionPool.close();
    }
}