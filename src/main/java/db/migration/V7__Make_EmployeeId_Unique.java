package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V7__Make_EmployeeId_Unique extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();

		String duplicates = "SELECT DISTINCT(i1.`InstructorId`), i1.`EmployeeId`"
				+ " FROM `Instructors` i1, `Instructors` i2"
				+ " WHERE i1.`InstructorId` > i2.`InstructorId`"
				+ " AND i1.`EmployeeId` = i2.`EmployeeId`";
		PreparedStatement statement0 = connection.prepareStatement(duplicates);

		try {
			ResultSet rs0 = statement0.executeQuery(duplicates);
			// Loop over duplicates and update associations
			while (rs0.next()) {
				int id = rs0.getInt("InstructorId");
				String employeeId = rs0.getString("EmployeeId");

				// Get the lowest InstructorId that belongs to the same instructor
				String replacementId = "SELECT MIN(i0.`InstructorId`) AS iid"
						+ " FROM IPA.`Instructors` i0 "
						+ " WHERE i0.`EmployeeId` = '" + employeeId + "'";
				PreparedStatement statement1 = connection.prepareStatement(replacementId);
				ResultSet rs1 = statement1.executeQuery(replacementId);
				rs1.next();

				// Update the association with the InstructorId found above
				String updateAssociation = "UPDATE `Instructors_has_Sections` ihs"
						+ " SET ihs.`Instructors_InstructorId` = " + rs1.getInt("iid")
						+ " WHERE ihs.`Instructors_InstructorId` = " + id;
				PreparedStatement statement2 = connection.prepareStatement(updateAssociation);
				statement2.execute();

				statement2.close();
				statement1.close();
			}

			// Delete duplicate entries
			String deleteDuplicates = "DELETE i1 FROM `Instructors` i1, `Instructors` i2"
					+ " WHERE i1.`InstructorId` > i2.`InstructorId`"
					+ " AND i1.`EmployeeId` = i2.`EmployeeId`";
			PreparedStatement statement3 = connection.prepareStatement(deleteDuplicates);
			statement3.execute();
			statement3.close();

			// Create the unique constraint
			String makeEmployeeIdUnique = "ALTER TABLE `Instructors`"
					+ " ADD UNIQUE INDEX `EmployeeId_UNIQUE` (`EmployeeId` ASC);";
			PreparedStatement statement4 = connection.prepareStatement(makeEmployeeIdUnique);
			statement4.execute();
			statement4.close();

		} finally {
			statement0.close();
		}		
	}

}
