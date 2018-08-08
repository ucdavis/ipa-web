package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V205__Persist_Implicit_SectionGroupCosts implements JdbcMigration {

	/**
	 * Currently, budgetScenarios would only persist sectionGroupCosts when a user wants to override a value from the schedule,
	 * And the sectionGroupCost would only store override values, null indicating no override.
	 * This migration will ensure that all sectionGroups have a corresponding sectionGroupCost and that any null fields are updated to store a snapshot of the current value
	 * @param connection
	 * @throws Exception
	 */
	@Override
	public void migrate(Connection connection) throws Exception {
		// For each sectionGroup
		// For each budgetScenario in the same schedule as the sectionGroup
		// Query for a sectionGroupCost that matches the sectionGroupId and budgetScenarioId
		// If found, update all null fields on the sectionGroupCost with current data (from sectionGroup and course)
		// If note found, create and mirror all fields (from sectionGroup and course)


		PreparedStatement psSectionGroups = connection.prepareStatement("SELECT sg.Id, sg.TermCode, c.ScheduleId FROM SectionGroups sg, Courses c WHERE sg.CourseId = c.Id;");
		connection.setAutoCommit(false);

		ResultSet rsSectionGroups = psSectionGroups.executeQuery();

		while(rsSectionGroups.next()) {
			Long sectionGroupId = rsSectionGroups.getLong("Id");
			String termCode = rsSectionGroups.getString("TermCode");
			Long scheduleId = rsSectionGroups.getLong("ScheduleId");

			PreparedStatement psBudgetScenarios = connection.prepareStatement("SELECT bs.Id FROM BudgetScenarios bs, Budgets b WHERE bs.BudgetId = b.Id AND b.ScheduleId = ?;");
			psBudgetScenarios.setLong(1, scheduleId);
			ResultSet rsBudgetScenarios = psBudgetScenarios.executeQuery();

			while(rsBudgetScenarios.next()) {
				Long budgetScenarioId = rsSectionGroups.getLong("Id");

				PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts sgc WHERE sgc.BudgetScenarioId = ? AND sgc.SectionGroupId = ?;");
				psSectionGroupCosts.setLong(1, budgetScenarioId);
				psSectionGroupCosts.setLong(2, sectionGroupId);
				ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();

				rsSectionGroupCosts.last();
				int rowCount = rsSectionGroupCosts.getRow();
				rsSectionGroupCosts.first();

				if (rowCount > 0) {
					while(rsBudgetScenarios.next()) {
						/* TODO: fill in any null fields */
						System.out.println("debug");
					}
				} else {
					/* TODO: Create a sectionGroupCost using relevant pieces of data */
					System.out.println("debug");

					// budgetScenarioId
					// sectionGroupId
					// taCount
					// readerCount

					// sectionCount
					// enrollment

					// instructorId
					// instructorTypeId
				}
			}
		}

		// Commit changes
		connection.commit();
	}
}
