package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;

public class V190__Assign_Instructor_Types implements JdbcMigration {

    /**
     * For every instructorTypeCost, looks at the deprecrated description field and associate to the matching instructorType.
     * Will also delete instructorTypeCosts with no amount set.
     * This is in preparation for dropping the description column and making instructorTypeCosts optional.
     *
     * @param connection
     * @throws Exception
     */
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psInstructorTypeCosts = connection.prepareStatement("SELECT * FROM `InstructorTypeCosts`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsInstructorTypeCosts = psInstructorTypeCosts.executeQuery();

            while (rsInstructorTypeCosts.next()) {
                Long id = rsInstructorTypeCosts.getLong("Id");
                String description = rsInstructorTypeCosts.getString("Description");
                Long cost = rsInstructorTypeCosts.getLong("Cost");

                // Delete instructorTypeCosts with null values
                if (cost == null) {
                    PreparedStatement psDeleteInstructorTypeCost = connection.prepareStatement(
                            "DELETE FROM `InstructorTypeCosts` WHERE `Id` = ?;"
                    );

                    psDeleteInstructorTypeCost.setLong(1, id);

                    psDeleteInstructorTypeCost.execute();
                    psDeleteInstructorTypeCost.close();
                }

                Long instructorTypeId = null;

                switch (description) {
                    case "Visiting Professor":
                    case "Visting professor":
                        instructorTypeId = 2L;
                        break;
                    case "Continuing Lecturer":
                        instructorTypeId = 5L;
                        break;
                    case "Unit 18 Pre-six":
                        instructorTypeId = 4L;
                        break;
                    case "Associate Instructor":
                        instructorTypeId = 3L;
                        break;
                    case "Emeriti":
                        instructorTypeId = 1L;
                        break;
                    case "Ladder Faculty":
                        instructorTypeId = 6L;
                        break;
                }

                PreparedStatement psSetInstructorTypeCost = connection.prepareStatement(
                    " UPDATE `InstructorTypeCosts` instructorTypeCost " +
                            " SET instructorTypeCost.`InstructorTypeId` = ? " +
                            " WHERE instructorTypeCost.`Id` = ?; "
                );

                psSetInstructorTypeCost.setLong(1, instructorTypeId);
                psSetInstructorTypeCost.setLong(2, id);
                psSetInstructorTypeCost.execute();
                psSetInstructorTypeCost.close();
            }

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psInstructorTypeCosts != null) {
                    psInstructorTypeCosts.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}