package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class V226_flag_preexisting_freeform_users implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psUsers = connection.prepareStatement("UPDATE Users SET Placeholder = 1 WHERE LoginId LIKE 'change%';");
        psUsers.execute();
        psUsers.close();
    }
}
