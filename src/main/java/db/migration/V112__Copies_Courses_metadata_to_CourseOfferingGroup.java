package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class V112__Copies_Courses_metadata_to_CourseOfferingGroup implements JdbcMigration {

	@Override
	public void migrate(Connection connection) throws Exception {
		// Add required metadata columns to CourseOfferingGroups
		PreparedStatement psAddMetadataColumns = connection.prepareStatement(
			" ALTER TABLE `CourseOfferingGroups`" +
			" ADD COLUMN `SubjectCode` VARCHAR(4) NOT NULL," +
			" ADD COLUMN `CourseNumber` VARCHAR(7) NOT NULL," +
			" ADD COLUMN `EffectiveTermCode` VARCHAR(6) NOT NULL;"
		);

		// Get all the CourseOfferingGroups
		PreparedStatement psCogs = connection.prepareStatement(
				"SELECT * FROM `CourseOfferingGroups`");
		ResultSet rsCogs = psCogs.executeQuery();

		try {
			psAddMetadataColumns.execute();

			// Loop over CourseOfferingGroups
			while(rsCogs.next()) {
				long courseOfferingGroupId = rsCogs.getLong("id");
				long courseId = rsCogs.getLong("CourseId");

				// Look for associated Course
				PreparedStatement psCourse = connection.prepareStatement(
					" SELECT * FROM `Courses` " +
					" WHERE `Courses`.`CourseId` = ?; "
				);
				psCourse.setLong(1, courseId);
				ResultSet rsCourse = psCourse.executeQuery();

				if (rsCourse.next()) {
					String subjectCode = rsCourse.getString("SubjectCode");
					String courseNumber = rsCourse.getString("CourseNumber");
					String effectiveTermCode = rsCourse.getString("EffectiveTermCode");

					// Set Course metadata on CourseOfferingGroup
					PreparedStatement psSetMetaData = connection.prepareStatement(
						" UPDATE `CourseOfferingGroups` cogs " +
						" SET cogs.`SubjectCode` = ?, " +
						"     cogs.`CourseNumber` = ?, " +
						"     cogs.`EffectiveTermCode` = ? " +
						" WHERE cogs.`id` = ?; "
					);
					psSetMetaData.setString(1, subjectCode);
					psSetMetaData.setString(2, courseNumber);
					psSetMetaData.setString(3, effectiveTermCode);
					psSetMetaData.setLong(4, courseOfferingGroupId);

					psSetMetaData.execute();
					psSetMetaData.close();
				} else {
					System.out.println("Course with ID " + courseId + "Does not exist");
				}

				psCourse.close();
			}


		} finally {
			rsCogs.close();
		}
	}

}
