package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.flywaydb.core.api.migration.Context;

public class V204__Fix_numeric_activities extends BaseJavaMigration {

  /**
   * Identify activities that are inappropriately connected to sections that are numeric,
   * And connect them instead to the parent sectionGroup
   * @param context
   * @throws Exception
   */
  @Override
  public void migrate(Context context) throws Exception {
    Connection connection = context.getConnection();

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
