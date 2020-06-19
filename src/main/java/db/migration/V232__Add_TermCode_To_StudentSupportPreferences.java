package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

public class V232__Add_TermCode_To_StudentSupportPreferences implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        // Add TermCode to track when Preference was created
        PreparedStatement psAddTermCodeColumnToStudentSupportPreferences =
            connection.prepareStatement(
                "ALTER TABLE `StudentSupportPreferences` ADD COLUMN `TermCode` VARCHAR(6) NOT NULL;");

        // Get SectionGroup TermCodes
        PreparedStatement psTermCodesQuery = connection.prepareStatement(
            "SELECT"
                + " StudentSupportPreferences.`Id` AS StudentSupportPreferenceId, "
                + " `TermCode`"
                + " FROM `StudentSupportPreferences` "
                + " JOIN `SectionGroups` "
                + " ON SectionGroups.`id` = StudentSupportPreferences.`SectionGroupId`; ");

        ResultSet rsTermCodes = psTermCodesQuery.executeQuery();

        try {
            psAddTermCodeColumnToStudentSupportPreferences.execute();

            while (rsTermCodes.next()) {
                long studentSupportPreferenceId = rsTermCodes.getLong("StudentSupportPreferenceId");
                String termCode = rsTermCodes.getString("TermCode");

                String setStudentSupportPreferenceTermCodeQuery =
                    "UPDATE `StudentSupportPreferences`"
                        + "SET TermCode = ? "
                        + "WHERE Id = ? ";
                PreparedStatement psSetStudentSupportPreferenceTermCode =
                    connection.prepareStatement(setStudentSupportPreferenceTermCodeQuery);
                psSetStudentSupportPreferenceTermCode.setString(1, termCode);
                psSetStudentSupportPreferenceTermCode.setLong(2, studentSupportPreferenceId);

                psSetStudentSupportPreferenceTermCode.execute();
                psSetStudentSupportPreferenceTermCode.close();
            }

        } finally {
            psAddTermCodeColumnToStudentSupportPreferences.close();
            rsTermCodes.close();
        }
    }

}
