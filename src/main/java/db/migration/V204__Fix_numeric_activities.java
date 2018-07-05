package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V204__Fix_numeric_activities implements JdbcMigration {

  /**
   * Identify activities that are inappropriately connected to sections that are numeric,
   * And connect them instead to the parent sectionGroup
   * @param connection
   * @throws Exception
   */
  @Override
  public void migrate(Connection connection) throws Exception {
    PreparedStatement psActivities = connection.prepareStatement("SELECT a.Id, s.sectionGroupId FROM Sections s, Activities a WHERE s.SequenceNumber > 0 AND a.SectionId IS NOT NULL AND a.sectionId = s.Id;");
    connection.setAutoCommit(false);

    ResultSet rsActivities = psActivities.executeQuery();

    while(rsActivities.next()) {
      Long activityId = rsActivities.getLong("Id");
      Long sectionGroupId = rsActivities.getLong("SectionGroupId");

      // Update Activity
      PreparedStatement psUpdateActivity = connection.prepareStatement(
          " UPDATE `Activities` SET `SectionId` = NULL, `SectionGroupId` = ? WHERE `Id` = ?;"
      );

      psUpdateActivity.setLong(1, sectionGroupId);
      psUpdateActivity.setLong(2, activityId);

      psUpdateActivity.execute();
      psUpdateActivity.close();
    }

    // Commit changes
    connection.commit();
  }
}
