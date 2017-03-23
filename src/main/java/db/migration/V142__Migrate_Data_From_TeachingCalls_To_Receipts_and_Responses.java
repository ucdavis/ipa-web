package db.migration;

        import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

        import java.sql.*;

public class V142__Migrate_Data_From_TeachingCalls_To_Receipts_and_Responses implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psTeachingCalls = connection.prepareStatement("SELECT * FROM `TeachingCalls`");

        try {
            connection.setAutoCommit(false);

            // Find TeachingCalls
            ResultSet rsTeachingCalls = psTeachingCalls.executeQuery();

            while (rsTeachingCalls.next()) {
                long teachingCallId = rsTeachingCalls.getLong("Id");

                long ShowUnavailabilities = rsTeachingCalls.getLong("ShowUnavailabilities");
                String message = rsTeachingCalls.getString("Message");
                String termsBlob = rsTeachingCalls.getString("TermsBlob");
                long scheduleId = rsTeachingCalls.getLong("ScheduleId");
                Date dueDate = rsTeachingCalls.getDate("DueDate");

                // STEP 1: Find Related teachingCallReceipts
                PreparedStatement psTeachingCallReceipts = connection.prepareStatement(
                    "SELECT * FROM `TeachingCallReceipts` WHERE `TeachingCallReceipts`.`TeachingCallId` = ?; "
                );

                psTeachingCallReceipts.setLong(1, teachingCallId);
                ResultSet rsTeachingCallReceipts = psTeachingCallReceipts.executeQuery();

                while (rsTeachingCallReceipts.next()) {
                    String warnedAt = rsTeachingCallReceipts.getString("WarnedAt");

                    // Set teachingCallReceipt values:
                    // Set message, termsBlob, showUnavail, scheduleId
                    PreparedStatement psSetReceiptMetadata = connection.prepareStatement(
                            " UPDATE `TeachingCallReceipts` receipt " +
                                    " SET receipt.`Message` = ?, " +
                                    "     receipt.`TermsBlob` = ?, " +
                                    "     receipt.`ShowUnavailabilities` = ?, " +
                                    "     receipt.`Schedules_ScheduleId` = ?, " +
                                    "     receipt.`DueDate` = ? " +
                                    " WHERE receipt.`TeachingCallId` = ?; "
                    );

                    psSetReceiptMetadata.setString(1, message);
                    psSetReceiptMetadata.setString(2, termsBlob);
                    psSetReceiptMetadata.setLong(3, ShowUnavailabilities);
                    psSetReceiptMetadata.setLong(4, scheduleId);
                    psSetReceiptMetadata.setDate(5, dueDate);
                    psSetReceiptMetadata.setLong(6, teachingCallId);
                    psSetReceiptMetadata.execute();
                    psSetReceiptMetadata.close();


                    // Potentially set NextContactAt
                    if (warnedAt != null) {
                        // TODO
                    }
                }

                psTeachingCallReceipts.close();


                // STEP 2: Find Related teachingCallResponses
                PreparedStatement psTeachingCallResponses = connection.prepareStatement(
                        "SELECT * FROM `TeachingCallResponses` WHERE `TeachingCallResponses`.`TeachingCallId` = ?; "
                );

                psTeachingCallResponses.setLong(1, teachingCallId);
                ResultSet rsTeachingCallResponses = psTeachingCallResponses.executeQuery();

                while (rsTeachingCallResponses.next()) {

                    // Set teachingCallResponse scheduleId
                    PreparedStatement psSetResponseMetadata = connection.prepareStatement(
                            " UPDATE `TeachingCallResponses` response " +
                                    " SET response.`Schedules_ScheduleId` = ? " +
                                    " WHERE response.`TeachingCallId` = ?; "
                    );

                    psSetResponseMetadata.setLong(1, scheduleId);
                    psSetResponseMetadata.setLong(2, teachingCallId);

                    psSetResponseMetadata.execute();
                    psSetResponseMetadata.close();
                }

                psTeachingCallResponses.close();
            } // End rsTeachingCall loop

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psTeachingCalls != null) {
                    psTeachingCalls.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}