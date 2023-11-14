package db.migration;

import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.*;
import org.flywaydb.core.api.migration.Context;

public class V166__Ensure_Activities_Of_Numeric_SectionGroups_Are_Associated_To_SectionGroup extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        // Find all activities
        // Is the activity associated to a section?
        // If the section has a numeric sequenceNumber, add a sectionGroup association and remove the section association

        PreparedStatement psActivities = connection.prepareStatement("SELECT * FROM `Activities`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsActivities = psActivities.executeQuery();

            while (rsActivities.next()) {
                long activityId = rsActivities.getLong("Id");
                long sectionId = rsActivities.getLong("SectionId");
                long sectionGroupId = rsActivities.getLong("SectionGroupId");

                if (sectionGroupId > 0) {
                    continue;
                }

                // Find Section
                PreparedStatement psSection = connection.prepareStatement(
                        "SELECT * FROM `Sections` WHERE `Id` = ? ;"
                );

                psSection.setLong(1, sectionId);

                ResultSet rsSection = psSection.executeQuery();
                rsSection.next();

                sectionGroupId = rsSection.getLong("SectionGroupId");

                // Find SectionGroup
                PreparedStatement psSectionGroup = connection.prepareStatement(
                    "SELECT * FROM `SectionGroups` WHERE `Id` = ? ;"
                );

                psSectionGroup.setLong(1, sectionGroupId);

                ResultSet rsSectionGroup = psSectionGroup.executeQuery();
                rsSectionGroup.next();

                long courseId = rsSectionGroup.getLong("CourseId");

                // Find Course
                PreparedStatement psCourse = connection.prepareStatement(
                        "SELECT * FROM `Courses` WHERE `Id` = ? ;"
                );

                psCourse.setLong(1, courseId);

                ResultSet rsCourse = psCourse.executeQuery();
                rsCourse.next();

                String sequencePattern = rsCourse.getString("SequencePattern");

                // If this is a letter based sectionGroup, then skip
                if (Utilities.isNumeric(sequencePattern) == false) {
                    continue;
                }

                // Add sectionGroup association
                PreparedStatement psAddSectionGroup = connection.prepareStatement(
                    " UPDATE `Activities` SET `SectionGroupId` = ? WHERE `Id` = ?;"
                );

                psAddSectionGroup.setLong(1, sectionGroupId);
                psAddSectionGroup.setLong(2, activityId);

                psAddSectionGroup.execute();
                psAddSectionGroup.close();

                // Remove section association
                PreparedStatement psRemoveSection = connection.prepareStatement(
                    " UPDATE `Activities` SET `SectionId` = NULL WHERE `Id` = ?;"
                );

                psRemoveSection.setLong(1, activityId);

                psRemoveSection.execute();
                psRemoveSection.close();
            } // End rsActivities loop

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