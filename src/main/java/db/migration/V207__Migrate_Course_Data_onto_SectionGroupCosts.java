package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V207__Migrate_Course_Data_onto_SectionGroupCosts implements JdbcMigration {

	/**
	 * Fills in the new fields on sectionGroupCosts to ensure they can continue to display properly,
	 * Even if the course/sectionGroup they were created from is deleted
	 * @param connection
	 * @throws Exception
	 */
	@Override
	public void migrate(Connection connection) throws Exception {
		System.out.println("[MIGRATION] 207 START");
		// For each sectionGroup
		// For each budgetScenario in the same schedule as the sectionGroup
		// Query for a sectionGroupCost that matches the sectionGroupId and budgetScenarioId
		// If found, update all null fields on the sectionGroupCost with current data (from sectionGroup and course)
		// If note found, create and mirror all fields (from sectionGroup and course)


		PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts;");
		connection.setAutoCommit(false);

		ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();
		int currentSectionGroupCost = 0;
		rsSectionGroupCosts.last();
		int totalSectionGroupCosts = rsSectionGroupCosts.getRow();
		rsSectionGroupCosts.first();

		while(rsSectionGroupCosts.next()) {
			currentSectionGroupCost += 1;

			// TODO: logic goes here

			if (currentSectionGroupCost % 1000 == 0) {
				System.out.println("sectionGroups processed: " + currentSectionGroupCost + " / " + totalSectionGroupCosts);
				System.out.println( Math.round(((currentSectionGroupCost * 1.0 / totalSectionGroupCosts) * 100)) + "%");
			}
		}

		System.out.println("[MIGRATION] 205 COMPLETE");

		// Commit changes
		connection.commit();
	}
}
