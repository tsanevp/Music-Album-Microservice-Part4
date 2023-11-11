package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ReviewController {

    public ReviewController() {
    }

    /**
     * Updates the mySQL database by incrementing the like count for the album. Has a UUID as a key.
     *
     * @param connection - A connection to the mySQL database.
     * @param uuid       - The UUID key to update the album like count for.
     * @return - The row count of the update. If the value is greater than 0, the update was successful.
     * @throws SQLException - Occurs if a database access error occurs.
     */
    public int addLike(Connection connection, String uuid) throws SQLException {
        String updateQuery = "UPDATE albumRequests SET numberOfLikes = numberOfLikes + 1 WHERE AlbumID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);

        // Set values for the prepared statement
        preparedStatement.setString(1, uuid);

        // Execute the statement
        return preparedStatement.executeUpdate();
    }

    /**
     * Updates the mySQL database by incrementing the dislike count for the album. Has a UUID as a key.
     *
     * @param connection - A connection to the mySQL database.
     * @param uuid       - The UUID key to update the album dislike count for.
     * @return - The row count of the update. If the value is greater than 0, the update was successful.
     * @throws SQLException - Occurs if a database access error occurs.
     */
    public int addDislike(Connection connection, String uuid) throws SQLException {
        String updateQuery = "UPDATE albumRequests SET numberOfDislikes = numberOfDislikes + 1 WHERE AlbumID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(updateQuery);

        // Set values for the prepared statement
        preparedStatement.setString(1, uuid);

        // Execute the statement
        return preparedStatement.executeUpdate();
    }
}
