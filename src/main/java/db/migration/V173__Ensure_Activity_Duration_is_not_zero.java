package db.migration;

import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import org.flywaydb.core.api.migration.Context;

public class V173__Ensure_Activity_Duration_is_not_zero extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        // Find all activities

        PreparedStatement psActivities = connection.prepareStatement("SELECT * FROM `Activities`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsActivities = psActivities.executeQuery();

            while (rsActivities.next()) {
                long activityId = rsActivities.getLong("Id");
                Time startTime = rsActivities.getTime("StartTime");
                Time endTime = rsActivities.getTime("EndTime");

                if (startTime == null || endTime == null) {
                    continue;
                }

                if (startTime.equals(endTime) ) {
                    // Add sectionGroup association
                    PreparedStatement psActivity = connection.prepareStatement(
                            " DELETE FROM `Activities` WHERE `Id` = ?;"
                    );

                    psActivity.setLong(1, activityId);
                    psActivity.execute();
                    psActivity.close();
                }
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