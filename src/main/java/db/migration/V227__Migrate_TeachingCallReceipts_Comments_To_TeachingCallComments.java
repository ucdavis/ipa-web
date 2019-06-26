package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V227__Migrate_TeachingCallReceipts_Comments_To_TeachingCallComments implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        // Create TeachingCallComments table
        String createTeachingCallComments = "CREATE TABLE IF NOT EXISTS `TeachingCallComments` ("
                + "`Id` INT(11) AUTO_INCREMENT PRIMARY KEY,"
                + "`Comment` TEXT,"
                + "`TeachingCallReceiptsId` INT(11),"
                + "FOREIGN KEY (`TeachingCallReceiptsId`)"
                + " REFERENCES `TeachingCallReceipts`(`id`));";

        PreparedStatement createTableStatement = connection.prepareStatement(createTeachingCallComments);

        // Get TeachingCallReceipts comments
        PreparedStatement commentsQuery = connection.prepareStatement(
            "SELECT"
                    + " TeachingCallReceipts.`Id` AS TeachingCallReceiptsId, "
                    + " CONCAT(`firstName`, ' ', `lastName`) AS Author, "
                    + " `Comment` "
                    + " FROM `TeachingCallReceipts` "
                    + " JOIN `Instructors` "
                    + " ON Instructors.`id` = TeachingCallReceipts.`InstructorId` "
                    + " WHERE `Comment` IS NOT NULL AND Comment<>''; ");

        ResultSet rsComments = commentsQuery.executeQuery();

        try {
            createTableStatement.execute();

            // Loop over combined comments and insert them into Receipts
            while(rsComments.next()) {
                long teachingCallReceiptsId = rsComments.getLong("teachingCallReceiptsId");
                String comment = rsComments.getString("comment");

                // Save combine comments in TeachingCallReceipts
                String insertTeachingCallCommentsQuery = "INSERT INTO `TeachingCallComments`"
                        + " (Comment, TeachingCallReceiptsId) "
                        + " VALUES (?, ?)";
                PreparedStatement insertTeachingCallCommentsStatement = connection.prepareStatement(insertTeachingCallCommentsQuery);
                insertTeachingCallCommentsStatement.setString(1, comment);
                insertTeachingCallCommentsStatement.setLong(2, teachingCallReceiptsId);

                insertTeachingCallCommentsStatement.execute();
                insertTeachingCallCommentsStatement.close();
            }

            // Drop the Comment column from TeachingCallResponses
            String dropColumnQuery = "ALTER TABLE `TeachingCallReceipts` DROP COLUMN `Comment`;";
            PreparedStatement dropColumnStatement = connection.prepareStatement(dropColumnQuery);
            dropColumnStatement.execute();
            dropColumnStatement.close();

        } finally {
            createTableStatement.close();
            rsComments.close();
        }
    }

}
