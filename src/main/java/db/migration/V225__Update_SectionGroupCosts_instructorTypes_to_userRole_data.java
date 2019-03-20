package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V225__Update_SectionGroupCosts_instructorTypes_to_userRole_data implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts WHERE InstructorId IS NOT NULL;");
        connection.setAutoCommit(false);

        ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();

        while(rsSectionGroupCosts.next()) {
            Long sectionGroupCostId = rsSectionGroupCosts.getLong("Id");
            Long sectionGroupCostInstructorTypeId = rsSectionGroupCosts.getLong("InstructorTypeId");

            PreparedStatement psInstructorTypes = connection.prepareStatement("SELECT " +
                " sectionGroupCost.instructorTypeId as sectionGroupInstructorType, userRole.InstructorTypeId as userRoleInstructorType, user.LoginId " +
                " FROM SectionGroupCosts sectionGroupCost, BudgetScenarios budgetScenario, Budgets budget, Schedules schedule, Instructors instructor, Users user, UserRoles userRole " +
                " WHERE sectionGroupCost.Id = ? " +
                " AND sectionGroupCost.InstructorId = instructor.Id " +
                " AND instructor.LoginId = user.LoginId " +
                " AND userRole.userId = user.Id " +
                " AND budgetScenario.Id = sectionGroupCost.BudgetScenarioId " +
                " AND budget.Id = budgetScenario.BudgetId " +
                " AND budget.ScheduleId = schedule.Id " +
                " AND schedule.WorkgroupId = userRole.workgroupId " +
                " AND userRole.RoleId = 15"
            );

            psInstructorTypes.setLong(1, sectionGroupCostId);
            ResultSet rsInstructorTypes = psInstructorTypes.executeQuery();

            if (rsInstructorTypes.first()) {
                Long userRoleInstructorTypeId = rsInstructorTypes.getLong("userRoleInstructorType");

                if (sectionGroupCostInstructorTypeId == userRoleInstructorTypeId) { continue; }

                PreparedStatement psUpdateSectionGroupCost = connection.prepareStatement(
                    " UPDATE SectionGroupCosts" +
                        " SET InstructorTypeId = ?" +
                        " WHERE Id = ?;"
                );

                psUpdateSectionGroupCost.setLong(1, userRoleInstructorTypeId);
                psUpdateSectionGroupCost.setLong(2, sectionGroupCostId);
                psUpdateSectionGroupCost.execute();
                psUpdateSectionGroupCost.close();
            }
        }

        connection.commit();
    }
}
