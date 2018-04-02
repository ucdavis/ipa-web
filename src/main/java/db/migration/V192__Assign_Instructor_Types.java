package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;

public class V192__Assign_Instructor_Types implements JdbcMigration {
    static Long INSTRUCTOR_ROLE = 15L;

    static Long EMERITI = 1L;
    static Long VISITING_PROFESSOR = 2L;
    static Long ASSOCIATE_INSTRUCTOR = 3L;
    static Long UNIT_18_PRE_SIX = 4L;
    static Long CONTINUING_LECTURER = 5L;
    static Long LADDER_FACULTY = 6L;
    static Long INSTRUCTOR = 7L;

    /**
     * For every instructorTypeCost, looks at the deprecrated description field and associate to the matching instructorType.
     * Will also delete instructorTypeCosts with no amount set.
     * This is in preparation for dropping the description column and making instructorTypeCosts optional.
     *
     * @param connection
     * @throws Exception
     */
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psInstructorTypeCosts = connection.prepareStatement("SELECT * FROM `InstructorTypeCosts`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsInstructorTypeCosts = psInstructorTypeCosts.executeQuery();

            while (rsInstructorTypeCosts.next()) {
                Long id = rsInstructorTypeCosts.getLong("Id");
                String description = rsInstructorTypeCosts.getString("Description");
                Long cost = rsInstructorTypeCosts.getLong("Cost");

                // Delete instructorTypeCosts with null values
                if (cost == null) {
                    PreparedStatement psDeleteInstructorTypeCost = connection.prepareStatement(
                            "DELETE FROM `InstructorTypeCosts` WHERE `Id` = ?;"
                    );

                    psDeleteInstructorTypeCost.setLong(1, id);

                    psDeleteInstructorTypeCost.execute();
                    psDeleteInstructorTypeCost.close();
                }

                Long instructorTypeId = null;

                switch (description) {
                    case "Visiting Professor":
                    case "Visting professor":
                        instructorTypeId = VISITING_PROFESSOR;
                        break;
                    case "Continuing Lecturer":
                        instructorTypeId = CONTINUING_LECTURER;
                        break;
                    case "Unit 18 Pre-six":
                        instructorTypeId = UNIT_18_PRE_SIX;
                        break;
                    case "Associate Instructor":
                        instructorTypeId = ASSOCIATE_INSTRUCTOR;
                        break;
                    case "Emeriti":
                        instructorTypeId = EMERITI;
                        break;
                    case "Ladder Faculty":
                        instructorTypeId = LADDER_FACULTY;
                        break;
                }

                // No value was set, so removing unneeded override object
                // Or the group reference in the description didn't map to a instructorType (example: 'The Staff' or 'Adjunct Professor')
                if (instructorTypeId == null || cost == null) {
                    PreparedStatement psSetInstructorTypeCost = connection.prepareStatement(
                            " DELETE FROM `InstructorTypeCosts` " +
                                    " WHERE `Id` = ?; "
                    );

                    psSetInstructorTypeCost.setLong(1, id);
                    psSetInstructorTypeCost.execute();
                    psSetInstructorTypeCost.close();

                }
                if (instructorTypeId != null) {
                    PreparedStatement psSetInstructorTypeCost = connection.prepareStatement(
                            " UPDATE `InstructorTypeCosts` instructorTypeCost " +
                                    " SET instructorTypeCost.`InstructorTypeId` = ? " +
                                    " WHERE instructorTypeCost.`Id` = ?; "
                    );

                    psSetInstructorTypeCost.setLong(1, instructorTypeId);
                    psSetInstructorTypeCost.setLong(2, id);
                    psSetInstructorTypeCost.execute();
                    psSetInstructorTypeCost.close();
                }
            }

            // If an instructorCost had an instructorTypeCostId,
            // Generate the relevant instructor userRole
            PreparedStatement psInstructorCosts = connection.prepareStatement("SELECT * FROM `InstructorCosts`");
            ResultSet rsInstructorCosts = psInstructorCosts.executeQuery();

            while (rsInstructorCosts.next()) {
                Long instructorCostId = rsInstructorCosts.getLong("Id");
                Long instructorTypeCostId = rsInstructorCosts.getLong("InstructorTypeCostId");
                Long budgetId = rsInstructorCosts.getLong("BudgetId");
                Long instructorId = rsInstructorCosts.getLong("InstructorId");

                if (instructorTypeCostId == null || instructorTypeCostId == 0) { continue; }

                // Get instructorTypeId
                Long instructorTypeId = null;

                PreparedStatement psInstructorTypeCost = connection.prepareStatement(
                        "SELECT * FROM InstructorTypeCosts WHERE InstructorTypeCosts.Id = ?;"
                );

                psInstructorTypeCost.setLong(1, instructorTypeCostId);
                ResultSet rsInstructorTypeCost = psInstructorTypeCost.executeQuery();

                while(rsInstructorTypeCost.next()) {
                    String description = rsInstructorTypeCost.getString("Description");

                    switch (description) {
                        case "Visiting Professor":
                        case "Visting professor":
                            instructorTypeId = VISITING_PROFESSOR;
                            break;
                        case "Continuing Lecturer":
                            instructorTypeId = CONTINUING_LECTURER;
                            break;
                        case "Unit 18 Pre-six":
                            instructorTypeId = UNIT_18_PRE_SIX;
                            break;
                        case "Associate Instructor":
                            instructorTypeId = ASSOCIATE_INSTRUCTOR;
                            break;
                        case "Emeriti":
                            instructorTypeId = EMERITI;
                            break;
                        case "Ladder Faculty":
                            instructorTypeId = LADDER_FACULTY;
                            break;
                        default:
                            instructorTypeId = INSTRUCTOR;
                            break;
                    }
                }

                // The type was un-identifiable
                if (instructorTypeId == null) { continue; }

                // Get workgroupId
                Long workgroupId = null;

                PreparedStatement psWorkgroup = connection.prepareStatement(
                        "SELECT Workgroups.Id FROM Workgroups, Schedules, Budgets WHERE Budgets.Id = ? AND Schedules.WorkgroupId = Workgroups.Id AND Budgets.ScheduleId = Schedules.Id;"
                );

                psWorkgroup.setLong(1, budgetId);
                ResultSet rsWorkgroup = psWorkgroup.executeQuery();

                while(rsWorkgroup.next()) {
                    workgroupId = rsWorkgroup.getLong("Id");
                }

                if (workgroupId == null || workgroupId == 0) {
                    System.err.println("For InstructorCostId: " + instructorCostId + ", workgroupId was null based on instructorId: " + instructorId);
                    continue;
                }

                // Get userId
                Long userId = null;

                PreparedStatement psUser = connection.prepareStatement(
                        "SELECT Users.Id FROM Users, Instructors WHERE Instructors.Id = ? AND LOWER(Users.LoginId) = LOWER(Instructors.LoginId);"
                );
                psUser.setLong(1, instructorId);
                ResultSet rsUser = psUser.executeQuery();

                while(rsUser.next()) {
                    userId = rsUser.getLong("Id");
                }

                if (userId == null || userId == 0) {
                    System.err.println("For InstructorCostId: " + instructorCostId + ", UserId was null based on instructorId: " + instructorId);
                    continue;
                }

                // Generate user role
                PreparedStatement psCreateUserRole = connection.prepareStatement(
                        "INSERT INTO `UserRoles` (UserId, WorkgroupId, RoleId, InstructorTypeId) " +
                                "VALUES (?, ?, ?, ?);"
                        , Statement.RETURN_GENERATED_KEYS
                );

                psCreateUserRole.setLong(1, userId);
                psCreateUserRole.setLong(2, workgroupId);
                psCreateUserRole.setLong(3, INSTRUCTOR_ROLE);
                psCreateUserRole.setLong(4, instructorTypeId);
                psCreateUserRole.execute();
            }

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psInstructorTypeCosts != null) {
                    psInstructorTypeCosts.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}