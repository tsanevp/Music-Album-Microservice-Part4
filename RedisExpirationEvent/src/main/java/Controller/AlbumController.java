package Controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class AlbumController {

    public AlbumController() {
    }

    /**
     * Updates the mySQL database with the albumRequest info passed. Has a UUID as a key.
     *
     * @param connection   - A connection to the mySQL database.
     * @param uuid         - The UUID key to store and access the album info by.
     * @param imageData    - Information on the image uploaded.
     * @param albumProfile - The album profile to upload to the database.
     * @return - The row count of the update. If the value is greater than 0, the update was successful.
     * @throws SQLException - Occurs if a database access error occurs.
     */
    public int postToDatabase(Connection connection, String uuid, String imageData, String albumProfile) throws SQLException {
        String insertQuery = "INSERT INTO albumRequests (AlbumID, ImageData, AlbumProfile, NumberOfLikes, NumberOfDislikes) VALUES (?, ?, ?, 0, 0)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        // Set values for the prepared statement
        preparedStatement.setString(1, uuid);
        preparedStatement.setString(2, imageData);
        preparedStatement.setString(3, albumProfile);

        // Execute the statement
        return preparedStatement.executeUpdate();
    }
}
