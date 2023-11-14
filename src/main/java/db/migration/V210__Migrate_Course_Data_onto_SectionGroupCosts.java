package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.flywaydb.core.api.migration.Context;

public class V210__Migrate_Course_Data_onto_SectionGroupCosts extends BaseJavaMigration {

	/**
	 * Fills in the new fields on sectionGroupCosts to ensure they can continue to display properly,
	 * Even if the course/sectionGroup they were created from is deleted
	 * @param context
	 * @throws Exception
	 */
	@Override
	public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

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

			rsCourse.close();

			PreparedStatement psSetReceiptMetadata = connection.prepareStatement(
				" UPDATE `SectionGroupCosts` receipt " +
					" SET receipt.`Title` = ?, " +
					"     receipt.`CourseNumber` = ?, " +
					"     receipt.`SubjectCode` = ?, " +
					"     receipt.`SequencePattern` = ?, " +
					"     receipt.`TermCode` = ?, " +
					"     receipt.`UnitsHigh` = ?, " +
					"     receipt.`UnitsLow` = ?, " +
					"     receipt.`EffectiveTermCode` = ? " +
					" WHERE receipt.`Id` = ?; "
			);

			psSetReceiptMetadata.setString(1, title);
			psSetReceiptMetadata.setString(2, courseNumber);
			psSetReceiptMetadata.setString(3, subjectCode);
			psSetReceiptMetadata.setString(4, sequencePattern);
			psSetReceiptMetadata.setString(5, termCode);
			psSetReceiptMetadata.setFloat(6, unitsHigh);
			psSetReceiptMetadata.setFloat(7, unitsLow);
			psSetReceiptMetadata.setString(8, effectiveTermCode);
			psSetReceiptMetadata.setLong(9, sectionGroupCostId);
			psSetReceiptMetadata.execute();
			psSetReceiptMetadata.close();
		}

		// Commit changes
		connection.commit();
	}
}
