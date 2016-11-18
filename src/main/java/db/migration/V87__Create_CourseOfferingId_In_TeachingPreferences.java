package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;


public class V87__Create_CourseOfferingId_In_TeachingPreferences implements JdbcMigration {

	@Override
	public void migrate(Connection connection) throws Exception {
		String createColumnQuery = "ALTER TABLE `TeachingPreferences`"
				+ " ADD COLUMN `CourseOfferings_CourseOfferingId` INT(11) NULL";
		PreparedStatement createColumnStatement = connection.prepareStatement(createColumnQuery);

		String teachingPreferencesQuery = "SELECT `TeachingPreferences`.* FROM `TeachingPreferences`";
		PreparedStatement teachingPreferencesStatement = connection.prepareStatement(teachingPreferencesQuery);

		try {
			createColumnStatement.execute();
			ResultSet rsCreateColumn = createColumnStatement.executeQuery(teachingPreferencesQuery);
			// Loop over sectionGroups and create courseOfferings accordingly
			while (rsCreateColumn.next()) {
				long sectionGroupId = rsCreateColumn.getLong("SectionGroups_SectionGroupId");

				// Get the matching courseOffering if exists
				String sectionGroupQuery = "SELECT sgs.*"
						+ " FROM `SectionGroups` sgs "
						+ " WHERE sgs.`SectionGroupId` = " + sectionGroupId
						+ " LIMIT 1";
				PreparedStatement sectionGroupStatement = connection.prepareStatement(sectionGroupQuery);
				ResultSet rsSectionGroup = sectionGroupStatement.executeQuery(sectionGroupQuery);

				long courseOfferingId;
				if (rsSectionGroup.next()) {

					// A matching courseOffering exists, get its id
					courseOfferingId = rsSectionGroup.getLong("CourseOfferings_CourseOfferingId");
					
					// Set the association between the teachingPreference and the courseOffering
					String setAssociationQuery = "UPDATE `TeachingPreferences` tps"
							+ " SET tps.`CourseOfferings_CourseOfferingId` = " + courseOfferingId
							+ " WHERE tps.`SectionGroups_SectionGroupId` = " + sectionGroupId;

					PreparedStatement setAssociationStatement = connection.prepareStatement(setAssociationQuery);
					setAssociationStatement.execute();
					setAssociationStatement.close();
				}

				sectionGroupStatement.close();
			}

		} finally {
			createColumnStatement.close();
			teachingPreferencesStatement.close();
		}
	}

}
