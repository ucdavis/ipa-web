package db.migration;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import java.sql.*;

public class V148__Migrate_Teaching_Assistant_UserRoles implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
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