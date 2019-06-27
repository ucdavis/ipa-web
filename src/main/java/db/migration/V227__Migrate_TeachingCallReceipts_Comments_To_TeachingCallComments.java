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
                + "`Id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "`Comment` TEXT,"
                + "`TeachingCallReceiptId` INT(11),"
                + "`UpdatedAt` TIMESTAMP NULL,"
                + "`CreatedAt` TIMESTAMP NULL,"
                + "`ModifiedBy` VARCHAR(16) NULL,"
                + "FOREIGN KEY (`TeachingCallReceiptId`)"
                + " REFERENCES `TeachingCallReceipts`(`id`));";

        PreparedStatement createTableStatement = connection.prepareStatement(createTeachingCallComments);

        // Get TeachingCallReceipts comments
        PreparedStatement commentsQuery = connection.prepareStatement(
            "SELECT"
                    + " TeachingCallReceipts.`Id` AS TeachingCallReceiptId, "
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
                long teachingCallReceiptId = rsComments.getLong("teachingCallReceiptId");
                String comment = rsComments.getString("comment");

                // Save combine comments in TeachingCallReceipts
                String insertTeachingCallCommentsQuery = "INSERT INTO `TeachingCallComments`"
                        + " (Comment, TeachingCallReceiptId) "
                        + " VALUES (?, ?)";
                PreparedStatement insertTeachingCallCommentsStatement = connection.prepareStatement(insertTeachingCallCommentsQuery);
                insertTeachingCallCommentsStatement.setString(1, comment);
                insertTeachingCallCommentsStatement.setLong(2, teachingCallReceiptId);

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
