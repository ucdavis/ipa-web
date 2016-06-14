package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;


public class V113__Copies_CourseOfferings_metadata_to_SectionGroups implements JdbcMigration {

	@Override
	public void migrate(Connection connection) throws Exception {
		// Add required metadata columns to SectionGroups
		PreparedStatement psAddMetadataColumns = connection.prepareStatement(
			" ALTER TABLE `SectionGroups`" +
			" ADD COLUMN `CourseId` INT(11) NOT NULL," +
			" ADD COLUMN `TermCode` VARCHAR(6) NOT NULL," +
			" ADD COLUMN `PlannedSeats` INT(11) NULL;"
		);

		// Get all the SectionGroups
		PreparedStatement psSgs = connection.prepareStatement(
				"SELECT * FROM `SectionGroups`");
		ResultSet rsSgs = psSgs.executeQuery();

		try {
			psAddMetadataColumns.execute();

			// Loop over CourseOfferingGroups
			while(rsSgs.next()) {
				long sectionGroupId = rsSgs.getLong("SectionGroupId");
				long courseOfferingId = rsSgs.getLong("CourseOfferings_CourseOfferingId");

				// Look for associated CourseOffering
				PreparedStatement psCourse = connection.prepareStatement(
					" SELECT * FROM `CourseOfferings` " +
					" WHERE `CourseOfferings`.`CourseOfferingId` = ?; "
				);
				psCourse.setLong(1, courseOfferingId);
				ResultSet rsCourse = psCourse.executeQuery();

				if (rsCourse.next()) {
					long courseId = rsCourse.getLong("CourseOfferingGroupId");
					String termCode = rsCourse.getString("TermCode");
					String plannedSeats = rsCourse.getString("SeatsTotal");

					// Set CourseOffering metadata on SectionGroup
					PreparedStatement psSetMetaData = connection.prepareStatement(
						" UPDATE `SectionGroups` sgs " +
						" SET sgs.`courseId` = ?, " +
						"     sgs.`TermCode` = ?, " +
						"     sgs.`PlannedSeats` = ? " +
						" WHERE sgs.`SectionGroupId` = ?; "
					);
					psSetMetaData.setLong(1, courseId);
					psSetMetaData.setString(2, termCode);
					psSetMetaData.setString(3, plannedSeats);
					psSetMetaData.setLong(4, sectionGroupId);

					psSetMetaData.execute();
					psSetMetaData.close();
				} else {
					System.out.println("CourseOffering with ID " + courseOfferingId + "Does not exist");
				}

				psCourse.close();
			}


		} finally {
			rsSgs.close();
		}
	}

}
