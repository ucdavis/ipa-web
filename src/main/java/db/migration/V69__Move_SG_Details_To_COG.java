package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public class V69__Move_SG_Details_To_COG implements JdbcMigration {

	@Override
	public void migrate(Connection connection) throws Exception {
		PreparedStatement statement0 = connection.prepareStatement("SELECT cogs.* FROM `CourseOfferingGroups` cogs");

		try {
			ResultSet cogRs = statement0.executeQuery();
			
			// Loop over COGs and copy data
			while (cogRs.next()) {
				long cogId = cogRs.getLong("id");

				// Get the lowest InstructorId that belongs to the same instructor
				String query = "SELECT sgs.`Title` AS Title,"
						+ " sgs.`UnitsLow` AS UnitsLow,"
						+ " sgs.`UnitsHigh` AS UnitsHigh,"
						+ " sgs.`CourseId` AS CourseId"
						+ " FROM `SectionGroups` sgs "
						+ " WHERE sgs.`CourseOfferingGroupId` = ?"
						+ " LIMIT 1";

				PreparedStatement statement1 = connection.prepareStatement(query);
				statement1.setLong(1, cogId);
				ResultSet rs = statement1.executeQuery();
				rs.next();

				// Copy SG data from above into the COG
				query = "UPDATE `CourseOfferingGroups` cogs"
						+ " SET cogs.`Title` = ?,"
						+ "     cogs.`UnitsLow` = ?,"
						+ "     cogs.`UnitsHigh` = ?,"
						+ "     cogs.`CourseId` = ?"
						+ " WHERE cogs.`id` = " + cogId;

				PreparedStatement statement2 = connection.prepareStatement(query);
				statement2.setString(1, rs.getString("Title"));
				statement2.setFloat(2, rs.getFloat("UnitsLow"));
				statement2.setFloat(3, rs.getFloat("UnitsHigh"));
				statement2.setLong(4, rs.getLong("CourseId"));

				statement2.execute();

				statement1.close();
				statement2.close();
			}
		} finally {
			statement0.close();
		}
	}
}
