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

		while(rsSectionGroupCosts.next()) {
			Long sectionGroupCostId = rsSectionGroupCosts.getLong("Id");
			Long sectionGroupId = rsSectionGroupCosts.getLong("SectionGroupId");

			String title = null;
			String subjectCode = null;
			String courseNumber = null;
			String effectiveTermCode = null;
			String sequencePattern = null;
			String termCode = null;
			Float unitsLow = null;
			Float unitsHigh = null;

			PreparedStatement psSectionGroup = connection.prepareStatement("SELECT * FROM SectionGroups sg WHERE sg.Id = ?;");
			psSectionGroup.setLong(1, sectionGroupId);
			ResultSet rsSectionGroup = psSectionGroup.executeQuery();

			Long courseId = null;

			while(rsSectionGroup.next()) {
				termCode = rsSectionGroup.getString("TermCode");
				courseId = rsSectionGroup.getLong("CourseId");
			}

			// SectionGroupCost had been orphaned and should be removed
			// No sectionGroup was associated with it, and hence no course data
			if (courseId == null) {
				PreparedStatement psDeleteSectionGroupCost = connection.prepareStatement("DELETE FROM `SectionGroupCosts` WHERE `Id` = ?;");
				psDeleteSectionGroupCost.setLong(1, sectionGroupCostId);
				psDeleteSectionGroupCost.execute();
				psDeleteSectionGroupCost.close();
				continue;
			}

			if (courseId == null) {
				System.out.println("debug");
			}

			PreparedStatement psCourse = connection.prepareStatement("SELECT * FROM Courses c WHERE c.Id = ?;");
			psCourse.setLong(1, courseId);
			ResultSet rsCourse = psCourse.executeQuery();

			while(rsCourse.next()) {
				title = rsCourse.getString("Title");
				courseNumber = rsCourse.getString("CourseNumber");
				subjectCode = rsCourse.getString("SubjectCode");
				sequencePattern = rsCourse.getString("SequencePattern");
				effectiveTermCode = rsCourse.getString("EffectiveTermCode");
				unitsLow = rsCourse.getFloat("UnitsLow");
				unitsHigh = rsCourse.getFloat("UnitsHigh");
			}

			PreparedStatement psUpdateSectionGroupCost = connection.prepareStatement(
				" UPDATE `SectionGroupCosts`" +
				" SET `Title` = ?," +
				" SET `CourseNumber` = ?," +
				" SET `SubjectCode` = ?," +
				" SET `SequencePattern` = ?," +
				" SET `EffectiveTermCode` = ?," +
				" SET `UnitsLow` = ?," +
				" SET `UnitsHigh` = ?," +
				" SET `TermCode` = ?," +
				" WHERE `Id` = ?;"
			);

			System.out.println("taco");

			psUpdateSectionGroupCost.setString(1, title);
			psUpdateSectionGroupCost.setString(2, courseNumber);
			psUpdateSectionGroupCost.setString(3, subjectCode);
			psUpdateSectionGroupCost.setString(4, "000");
			psUpdateSectionGroupCost.setString(5, effectiveTermCode);
			psUpdateSectionGroupCost.setFloat(6, unitsLow);
			psUpdateSectionGroupCost.setFloat(7, unitsHigh);
			psUpdateSectionGroupCost.setString(8, termCode);
			psUpdateSectionGroupCost.setLong(9, sectionGroupCostId);

			psUpdateSectionGroupCost.execute();
			psUpdateSectionGroupCost.close();
		}

		// Commit changes
		connection.commit();
	}
}
