package db.migration;

import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.Calendar;
import java.util.Date;

public class V173__Ensure_Activity_Duration_is_not_zero implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        try {
            PreparedStatement psActivities = connection.prepareStatement("Update Activities SET EndTime = DATE_ADD(EndTime, INTERVAL 50 MINUTE) WHERE StartTime = EndTime");

            psActivities.execute();
            psActivities.close();
        } catch(SQLException e) {
            e.printStackTrace();
            return;
        }
    }
}