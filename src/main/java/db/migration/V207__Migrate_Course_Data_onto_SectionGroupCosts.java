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
		PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts;");
		connection.setAutoCommit(false);

		ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();
		int currentSectionGroupCost = 0;
		rsSectionGroupCosts.last();
		int totalSectionGroupCosts = rsSectionGroupCosts.getRow();
		rsSectionGroupCosts.first();

		while(rsSectionGroupCosts.next()) {
			Long sectionGroupCostId = rsSectionGroupCosts.getLong("Id");
			Long sectionGroupId = rsSectionGroupCosts.getLong("SectionGroupId");

			currentSectionGroupCost += 1;

			String title = null;
			String subjectCode = null;
			String courseNumber = null;
			String effectiveTermCode = null;
			String sequencePattern = null;
			String termCode = null;
			Float unitsLow = null;
			Float unitsHigh = null;
			Boolean disabled = false;

			PreparedStatement psSectionGroup = connection.prepareStatement("SELECT * FROM SectionGroups sg WHERE sg.Id = ?;");
			psSectionGroup.setLong(1, sectionGroupId);
			ResultSet rsSectionGroup = psSectionGroup.executeQuery();

			Long courseId = null;
			while(rsSectionGroup.next()) {
				termCode = rsSectionGroup.getString("TermCode");
				courseId = rsSectionGroup.getLong("CourseId");

				PreparedStatement psCourse = connection.prepareStatement("SELECT * FROM Courses c WHERE c.Id = ?;");
				psCourse.setLong(1, courseId);
				ResultSet rsCourse = psCourse.executeQuery();

				while(rsCourse.next()) {
					title = rsSectionGroupCosts.getString("Title");
					courseNumber = rsSectionGroupCosts.getString("CourseNumber");
					subjectCode = rsSectionGroupCosts.getString("SubjectCode");
					sequencePattern = rsSectionGroupCosts.getString("SequencePattern");
					effectiveTermCode = rsSectionGroupCosts.getString("EffectiveTermCode");
					unitsLow = rsSectionGroupCosts.getFloat("UnitsLow");
					unitsHigh = rsSectionGroupCosts.getFloat("UnitsHigh");
				}

				PreparedStatement psUpdateSectionGroupCost = connection.prepareStatement(
					" UPDATE `SectionGroupCosts`" +
					" SET `Title` = ?," +
					" SET `CourseNumber` = ?," +
					" SET `SubjectCode` = ?," +
					" SET `SequenceNumber` = ?," +
					" SET `EffectiveTermCode` = ?," +
					" SET `UnitsLow` = ?," +
					" SET `UnitsHigh` = ?," +
					" SET `TermCode` = ?," +
					" WHERE `Id` = ?;"
				);

				psUpdateSectionGroupCost.setString(1, title);
				psUpdateSectionGroupCost.setString(2, courseNumber);
				psUpdateSectionGroupCost.setString(3, subjectCode);
				psUpdateSectionGroupCost.setString(4, sequencePattern);
				psUpdateSectionGroupCost.setString(5, effectiveTermCode);
				psUpdateSectionGroupCost.setFloat(6, unitsLow);
				psUpdateSectionGroupCost.setFloat(7, unitsHigh);
				psUpdateSectionGroupCost.setString(8, termCode);
				psUpdateSectionGroupCost.setLong(9, sectionGroupCostId);

				psUpdateSectionGroupCost.execute();
				psUpdateSectionGroupCost.close();

				if (currentSectionGroupCost % 1000 == 0) {
					System.out.println("sectionGroups processed: " + currentSectionGroupCost + " / " + totalSectionGroupCosts);
					System.out.println(Math.round(((currentSectionGroupCost * 1.0 / totalSectionGroupCosts) * 100)) + "%");
				}
			}
		}

		// Commit changes
		connection.commit();
	}
}
