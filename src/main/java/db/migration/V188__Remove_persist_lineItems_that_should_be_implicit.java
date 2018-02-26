package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.math.BigDecimal;
import java.sql.*;

public class V188__Remove_persist_lineItems_that_should_be_implicit implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        // Find all lineItems
        PreparedStatement psLineItems = connection.prepareStatement("SELECT * FROM `LineItems`");

        try {
            connection.setAutoCommit(false);

            ResultSet rsLineItems = psLineItems.executeQuery();

            String buyoutDescription = "Buyout Funds for";
            String workDescription = "Work-Life Balance Funds for";

            while (rsLineItems.next()) {
                long lineItemId = rsLineItems.getLong("Id");
                BigDecimal amount = rsLineItems.getBigDecimal("amount");
                String description = rsLineItems.getString("description");

                // Should not delete if description doesn't match auto-generated pattern
                // Look for string 'buyout funds for' or 'work-life balance funds for
                if (description.contains(buyoutDescription) == false && description.contains(workDescription) == false) {
                    continue;
                }

                // Should not delete if a cost was set
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    continue;
                }

                // Should not delete if comments were made
                PreparedStatement psComments = connection.prepareStatement("SELECT COUNT(*) AS commentCount FROM `LineItemComments` WHERE `LineItemId` = ?");
                psComments.setLong(1, lineItemId);

                ResultSet rsComments = psComments.executeQuery();
                rsComments.next();

                if (rsComments.getLong("commentCount") > 0) {
                    continue;
                }

                psComments.close();

                // Delete lineItem
                PreparedStatement psDeleteLineItem = connection.prepareStatement(
                        "DELETE FROM `LineItems` WHERE `Id` = ?;"
                );

                psDeleteLineItem.setLong(1, lineItemId);

                psDeleteLineItem.execute();
                psDeleteLineItem.close();
            }

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psLineItems != null) {
                    psLineItems.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}