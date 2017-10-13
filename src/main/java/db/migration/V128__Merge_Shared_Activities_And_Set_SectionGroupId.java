package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by okadri on 8/30/16.
 */
public class V128__Merge_Shared_Activities_And_Set_SectionGroupId implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psSectionGroupsWithSharedActivities = null;

        try {
            connection.setAutoCommit(false);

            // Add sectionGroupId column to Activities
            PreparedStatement psAddSectionGroupIdColumn = connection.prepareStatement(
                    " ALTER TABLE `Activities` " +
                            " ADD COLUMN `SectionGroupId` INT(11) NULL," +
                            " CHANGE COLUMN `SectionId` `SectionId` INT(11) NULL," +
                            " ADD CONSTRAINT `fk_Activities_SectionGroups` " +
                            "  FOREIGN KEY (`SectionGroupId`) " +
                            "  REFERENCES `SectionGroups` (`Id`) " +
                            "  ON DELETE NO ACTION " +
                            "  ON UPDATE NO ACTION; "
            );
            psAddSectionGroupIdColumn.execute();
            psAddSectionGroupIdColumn.close();

            // Find SectionGroups with shared Activities
            psSectionGroupsWithSharedActivities = connection.prepareStatement(
                    " select DISTINCT(sg.id)" +
                            " from `SectionGroups` sg, `Sections` s, `Activities` a " +
                            " where sg.`Id` = s.`SectionGroupId` " +
                            " AND s.`Id` = a.`SectionId` " +
                            " AND a.`Shared` = true; "
            );
            ResultSet rsSectionGroupsWithSharedActivities = psSectionGroupsWithSharedActivities.executeQuery();

            while (rsSectionGroupsWithSharedActivities.next()) {
                long sectionGroupId = rsSectionGroupsWithSharedActivities.getLong("id");

                // Each sectionGroup with shared activities has two steps
                // Step 1: Set the sectionGroupId on the first unique shared activities and clear the sectionId
                PreparedStatement psUniqueSharedActivities = connection.prepareStatement(
                        "SELECT MIN(a.`Id`) AS activityId " +
                                " FROM `Activities` a, `Sections` s " +
                                " WHERE a.`SectionId` = s.`Id` " +
                                " AND a.`Shared` = TRUE " +
                                " AND s.`SectionGroupId` = ? " +
                                " GROUP BY a.`ActivityTypeCode`, a.`StartTime`, " +
                                "  a.`EndTime`, a.`Frequency`, a.`DayIndicator`, " +
                                "  a.`LocationId`, a.`IsVirtual`"
                );
                psUniqueSharedActivities.setLong(1, sectionGroupId);
                ResultSet rsUniqueSharedActivities = psUniqueSharedActivities.executeQuery();

                while (rsUniqueSharedActivities.next()) {
                    long activityId = rsUniqueSharedActivities.getLong("activityId");

                    PreparedStatement psSetSectionGroupId = connection.prepareStatement(
                            " UPDATE `Activities` a " +
                                    " SET a.`SectionGroupId` = ?, " +
                                    "     a.`SectionId` = NULL " +
                                    " WHERE a.`id` = ?; "
                    );
                    psSetSectionGroupId.setLong(1, sectionGroupId);
                    psSetSectionGroupId.setLong(2, activityId);

                    psSetSectionGroupId.execute();
                    psSetSectionGroupId.close();
                }
                psUniqueSharedActivities.close();

                // Step 2: Delete the rest of the shared activities for that sectionGroup
                PreparedStatement psDeleteOtherSharedActivities = connection.prepareStatement(
                        "DELETE `Activities` FROM `Activities`, `Sections` " +
                                " WHERE `Activities`.`SectionId` = `Sections`.`Id` " +
                                " AND `Sections`.`SectionGroupId` = ? " +
                                " AND `Activities`.`SectionGroupId` IS NULL " +
                                " AND `Activities`.`Shared` = TRUE; "
                );
                psDeleteOtherSharedActivities.setLong(1, sectionGroupId);

                psDeleteOtherSharedActivities.execute();
                psDeleteOtherSharedActivities.close();

            }

            // Drop Shared column from Activities
            PreparedStatement psDropSharedColumn = connection.prepareStatement(
                    " ALTER TABLE `Activities` DROP COLUMN `Shared`;"
            );
            psDropSharedColumn.execute();
            psDropSharedColumn.close();

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psSectionGroupsWithSharedActivities != null) {
                    psSectionGroupsWithSharedActivities.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
