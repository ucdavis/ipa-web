package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class V199__Migrate_AI_placeholder_To_TeachingAssignment implements JdbcMigration {

    /**
     * Migrates AI placeholders into new teachingAssignments with instructorType 'AI'
     * @param connection
     * @throws Exception
     */
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psSectionGroups = connection.prepareStatement("SELECT * FROM SectionGroups WHERE ShowPlaceholderAI = 1");

        try {
            connection.setAutoCommit(false);
            ResultSet rsSectionGroups = psSectionGroups.executeQuery();

            while(rsSectionGroups.next()) {
                Long teachingAssignmentId = rsSectionGroups.getLong("Id");
                Long sectionGroupId = rsSectionGroups.getLong("Id");
                String termCode = rsSectionGroups.getString("TermCode");

                // Get schedule
                Long scheduleId = null;

                PreparedStatement psSchedule = connection.prepareStatement(
                        "SELECT Schedules.Id FROM Schedules, Courses, SectionGroups WHERE SectionGroups.Id = ? AND SectionGroups.CourseId = Courses.Id AND Courses.ScheduleId = Schedules.Id;"
                );
                psSchedule.setLong(1, sectionGroupId);
                ResultSet rsSchedule = psSchedule.executeQuery();

                while(rsSchedule.next()) {
                    scheduleId = rsSchedule.getLong("Id");
                }


                // TermCode
                // sectionGroupId
                // ScheduleId
                // Priority = 1
                // Approved = 1
                // InstructorTypeId = 3

                // Create new teachingAssignment
                PreparedStatement psCreateTeachingAssignment = connection.prepareStatement(
                        "INSERT INTO `TeachingAssignments` (Priority, TermCode, ScheduleId, Approved, SectionGroupId, InstructorTypeId)" +
                        "VALUES (?, ?, ?, ?, ?, ?);"
                );

                psCreateTeachingAssignment.setLong(1, 1);
                psCreateTeachingAssignment.setString(2, termCode);
                psCreateTeachingAssignment.setLong(3, scheduleId);
                psCreateTeachingAssignment.setBoolean(4, true);
                psCreateTeachingAssignment.setLong(5, sectionGroupId);
                psCreateTeachingAssignment.setLong(6, 3);

                psCreateTeachingAssignment.execute();
                psCreateTeachingAssignment.close();
            }

            // Commit changes
            connection.commit();
        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psSectionGroups != null) {
                    psSectionGroups.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}