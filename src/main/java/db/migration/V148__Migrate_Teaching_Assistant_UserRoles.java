package db.migration;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import java.sql.*;
import org.flywaydb.core.api.migration.Context;

public class V148__Migrate_Teaching_Assistant_UserRoles extends BaseJavaMigration {
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();

        PreparedStatement psUserRoles = connection.prepareStatement("SELECT * FROM `UserRoles` WHERE `RoleId` = ? OR `RoleId` = ?;");
        psUserRoles.setLong(1, 6L);
        psUserRoles.setLong(2, 7L);

        try {
            connection.setAutoCommit(false);

            // Find UserRoles
            ResultSet rsUserRoles = psUserRoles.executeQuery();

            while (rsUserRoles.next()) {
                long userRoleId = rsUserRoles.getLong("Id");

                // Update role
                PreparedStatement psUpdateUserRole = connection.prepareStatement(
                        " UPDATE `UserRoles` SET `RoleId` = ? WHERE `Id` = ?;"
                );

                psUpdateUserRole.setLong(1, 13L);
                psUpdateUserRole.setLong(2, userRoleId);

                psUpdateUserRole.execute();
                psUpdateUserRole.close();

            }

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psUserRoles != null) {
                    psUserRoles.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}