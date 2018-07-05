package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V204__Fix_orphaned_activities implements JdbcMigration {

  /**
   * Identify activities that are inappropriately connected to sections that are numeric,
   * And connect them instead to the parent sectionGroup
   * @param connection
   * @throws Exception
   */
  @Override
  public void migrate(Connection connection) throws Exception {

    // Look at all activities with a sectionId
    // If they have a numeric sequence pattern,
    // Find their sectionGroupId,
    // Set the sectionGroup and null the section, and save


    PreparedStatement psActivities = connection.prepareStatement("SELECT a.Id, s.sectionGroupId FROM Sections s, Activities a WHERE s.SequenceNumber > 0 AND a.SectionId IS NOT NULL AND a.sectionId = s.Id;");
    connection.setAutoCommit(false);

    ResultSet rsActivities = psActivities.executeQuery();

    while(rsActivities.next()) {
      Long activityId = rsActivities.getLong("Id");
      Long sectionGroupId = rsActivities.getLong("SectionGroupId");

      // Get activity
      PreparedStatement psActivity = connection.prepareStatement(
          "SELECT * " +
              "FROM Activities " +
              "WHERE Id = ?;"
      );

      psActivity.setLong(1, activityId);
      ResultSet rsActivity = psActivity.executeQuery();

      while (rsActivity.next()) {
        // Update activity
      }

      // Commit changes
      connection.commit();
    }
  }
}
