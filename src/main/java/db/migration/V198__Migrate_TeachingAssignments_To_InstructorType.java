package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class V198__Migrate_TeachingAssignments_To_InstructorType implements JdbcMigration {
    // Instructor Types
    public static final long INSTRUCTOR = 7L;

    /**
     * Calculate instructorTypes for pre-existing teachingAssignments.
     * @param connection
     * @throws Exception
     */
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psTeachingAssignments = connection.prepareStatement("SELECT * FROM `TeachingAssignments`");

        try {
            connection.setAutoCommit(false);
            ResultSet rsTeachingAssignments = psTeachingAssignments.executeQuery();

            while(rsTeachingAssignments.next()) {
                Long teachingAssignmentId = rsTeachingAssignments.getLong("Id");
                Long instructorId = rsTeachingAssignments.getLong("InstructorId");
                Long scheduleId = rsTeachingAssignments.getLong("ScheduleId");

                // Get workgroup
                Long workgroupId = null;

                PreparedStatement psWorkgroup = connection.prepareStatement(
                    "SELECT Workgroups.Id FROM Workgroups, Schedules WHERE Schedules.Id = ? AND Schedules.WorkgroupId = Workgroups.Id;"
                );
                psWorkgroup.setLong(1, scheduleId);
                ResultSet rsWorkgroup = psWorkgroup.executeQuery();

                while(rsWorkgroup.next()) {
                    workgroupId = rsWorkgroup.getLong("Id");
                }

                // Get instructorType
                Long instructorTypeId = null;

                // If user based, look at userRoles to calculate
                PreparedStatement psRoles = connection.prepareStatement(
                    "SELECT UserRoles.InstructorTypeId " +
                    "FROM Instructors, Users, UserRoles " +
                    "WHERE Instructors.Id = ? " +
                    "AND Instructors.LoginId = Users.LoginId " +
                    "AND UserRoles.UserId = Users.Id " +
                    "AND UserRoles.WorkgroupId = ?"
                );

                psRoles.setLong(1, instructorId);
                psRoles.setLong(2, workgroupId);

                ResultSet rsRoles = psRoles.executeQuery();

                while(rsRoles.next()) {
                    Long slotInstructorTypeId = rsRoles.getLong("InstructorTypeId");

                    if (slotInstructorTypeId != null) {
                        instructorTypeId = slotInstructorTypeId;
                    }
                }

                // If relevant userRole wasn't found, set instructorType to the generic 'instructor' type.
                if (instructorTypeId == null || instructorTypeId == 0) {
                    instructorTypeId = INSTRUCTOR;
                }

                // Update TeachingAssignment with instructorType
                PreparedStatement psUpdateTeachingAssignment = connection.prepareStatement("UPDATE TeachingAssignments Set InstructorTypeId = ? WHERE TeachingAssignments.Id = ?");

                psUpdateTeachingAssignment.setLong(1, instructorTypeId);
                psUpdateTeachingAssignment.setLong(2, teachingAssignmentId);

                psUpdateTeachingAssignment.execute();
                psUpdateTeachingAssignment.close();
            }

            // Commit changes
            connection.commit();
        } finally {
            if (psTeachingAssignments != null) {
                psTeachingAssignments.close();
            }
        }
    }
}
