package database.migrations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;


public class V73__Create_CourseOfferingId_In_SectionGroups implements JdbcMigration {

	@Override
	public void migrate(Connection connection) throws Exception {
		String createColumnQuery = "ALTER TABLE `SectionGroups`"
				+ " ADD COLUMN `CourseOfferings_CourseOfferingId` INT(11) NULL";
		PreparedStatement createColumnStatement = connection.prepareStatement(createColumnQuery);

		String sectionGroupsQuery = "SELECT `SectionGroups`.* FROM `SectionGroups`";
		PreparedStatement sectionGroupsStatement = connection.prepareStatement(sectionGroupsQuery);
		
		try {
			createColumnStatement.execute();
			ResultSet rsCreateColumn = createColumnStatement.executeQuery(sectionGroupsQuery);

			// Loop over sectionGroups and create courseOfferings accordingly
			while (rsCreateColumn.next()) {
				long sgId = rsCreateColumn.getLong("SectionGroupId");
				long cogId = rsCreateColumn.getLong("CourseOfferingGroupId");
				String termCode = rsCreateColumn.getString("TermCode");

				// Delete a sectionGroup if it has no termCode
				if (termCode == null || termCode.isEmpty() || termCode.equals("NULL")) {
					String deleteEmptySectionGroupQuery = "DELETE FROM `SectionGroups` WHERE `SectionGroups`.`SectionGroupId` = " + sgId;
					PreparedStatement deleteEmptySectionGroupStatement = connection.prepareStatement(deleteEmptySectionGroupQuery);
					deleteEmptySectionGroupStatement.execute();
					deleteEmptySectionGroupStatement.close();
					continue;
				}

				// Get the matching courseOffering if exists
				String courseOfferingQuery = "SELECT cos.*"
						+ " FROM `CourseOfferings` cos "
						+ " WHERE cos.`CourseOfferingGroupId` = " + cogId
						+ " AND cos.`TermCode` = " + termCode
						+ " LIMIT 1";
				PreparedStatement courseOfferingStatement = connection.prepareStatement(courseOfferingQuery);
				ResultSet rsCourseOffering = courseOfferingStatement.executeQuery(courseOfferingQuery);

				long coId;
				if (rsCourseOffering.next()) {

					// A matching courseOffering exists, get its id
					coId = rsCourseOffering.getLong("CourseOfferingId");
				} else {

					// Need to create a new courseOffering
					String createCourseOfferingQuery = "INSERT INTO `CourseOfferings`"
							+ " ( `TermCode`, `CourseOfferingGroupId` )"
							+ " VALUES ( '" + termCode + "', " + cogId + " )";
					PreparedStatement createCourseOfferingStatement = connection.prepareStatement(
							createCourseOfferingQuery, Statement.RETURN_GENERATED_KEYS);
					createCourseOfferingStatement.execute();
					ResultSet rsCreateCourseOffering = createCourseOfferingStatement.getGeneratedKeys();

					rsCreateCourseOffering.next();
					coId = rsCreateCourseOffering.getLong(1);
					createCourseOfferingStatement.close();
				}

				// Set the association between the sectionGroup and the courseOffering
				String setAssociationQuery = "UPDATE `SectionGroups` sgs"
						+ " SET sgs.`CourseOfferings_CourseOfferingId` = " + coId
						+ " WHERE sgs.`SectionGroupId` = " + sgId;
				PreparedStatement setAssociationStatement = connection.prepareStatement(setAssociationQuery);
				setAssociationStatement.execute();
				setAssociationStatement.close();

				courseOfferingStatement.close();
			}

		} finally {
			createColumnStatement.close();
			sectionGroupsStatement.close();
		}		
	}

}
