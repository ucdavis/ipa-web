package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V199__Migrate_AI_placeholder_To_TeachingAssignment implements JdbcMigration {
    static Long ASSOCIATE_INSTRUCTOR = 3L;

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
                    "SELECT Schedules.Id " +
                    "FROM Schedules, Courses, SectionGroups " +
                    "WHERE SectionGroups.Id = ? " +
                    "AND SectionGroups.CourseId = Courses.Id " +
                    "AND Courses.ScheduleId = Schedules.Id;"
                );
                psSchedule.setLong(1, sectionGroupId);
                ResultSet rsSchedule = psSchedule.executeQuery();

                while(rsSchedule.next()) {
                    scheduleId = rsSchedule.getLong("Id");
                }



                // Create new teachingAssignment
                PreparedStatement psCreateTeachingAssignment = connection.prepareStatement(
                        "INSERT INTO `TeachingAssignments` (Priority, TermCode, ScheduleId, Approved, SectionGroupId, InstructorTypeId)" +
                        "VALUES (?, ?, ?, ?, ?, ?);"
                );

                // Priority = 1
                psCreateTeachingAssignment.setLong(1, 1);
                // TermCode
                psCreateTeachingAssignment.setString(2, termCode);
                // ScheduleId
                psCreateTeachingAssignment.setLong(3, scheduleId);
                // Approved = 1
                psCreateTeachingAssignment.setBoolean(4, true);
                // SectionGroupId
                psCreateTeachingAssignment.setLong(5, sectionGroupId);
                // InstructorTypeId
                psCreateTeachingAssignment.setLong(6, ASSOCIATE_INSTRUCTOR);

                psCreateTeachingAssignment.execute();
                psCreateTeachingAssignment.close();
            }

            // Commit changes
            connection.commit();
        } finally {
            if (psSectionGroups != null) {
                psSectionGroups.close();
            }
        }
    }
}
