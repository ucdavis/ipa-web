package db.migration;

import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;

public class V168__Ensure_Activity_frequency_is_not_zero implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        // Find all activities

        PreparedStatement psActivities = connection.prepareStatement("SELECT * FROM `Activities`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsActivities = psActivities.executeQuery();

            while (rsActivities.next()) {
                long activityId = rsActivities.getLong("Id");
                long frequency = rsActivities.getLong("Frequency");

                if (frequency > 0) {
                    continue;
                }

                // Add sectionGroup association
                PreparedStatement psActivity = connection.prepareStatement(
                        " UPDATE `Activities` SET `Frequency` = 1 WHERE `Id` = ?;"
                );

                psActivity.setLong(1, activityId);
                psActivity.execute();
                psActivity.close();
            }

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psActivities != null) {
                    psActivities.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}