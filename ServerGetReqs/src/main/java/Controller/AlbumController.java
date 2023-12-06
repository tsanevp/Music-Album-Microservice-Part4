package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AlbumController {

    public AlbumController() {
    }

    /**
     * Queries mySQL database for an album that matches the passed albumId identifier.
     *
     * @param connection - A connection to the mySQL database.
     * @param albumId    - The album id to search for in the database.
     * @return - A result set with the query results. May or may not contain the desired album info.
     * @throws SQLException - Occurs if a database access error occurs.
     */
    public ResultSet getAlbumProfile(Connection connection, String albumId) throws SQLException {
        String selectQuery = "SELECT * FROM albumRequests WHERE AlbumID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);

        // Set values and execute the prepared statement
        preparedStatement.setString(1, albumId);
        return preparedStatement.executeQuery();
    }
}
