package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.flywaydb.core.api.migration.Context;

public class V217__Fix_sectionGroupCost_units extends BaseJavaMigration {

    /**
     * Fixes sectionGroupCosts that were in-correctly created without units
     * @param context
     * @throws Exception
     */
    @Override
    public void migrate(Context context) throws Exception {
        Connection connection = context.getConnection();
        PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts WHERE UnitsHigh IS NULL AND UnitsLow IS NULL;");
        connection.setAutoCommit(false);

        ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();

        while(rsSectionGroupCosts.next()) {
            Long sectionGroupCostId = rsSectionGroupCosts.getLong("Id");
            String subjectCode = rsSectionGroupCosts.getString("SubjectCode");
            String courseNumber = rsSectionGroupCosts.getString("CourseNumber");
            String effectiveTermCode = rsSectionGroupCosts.getString("EffectiveTermCode");

            PreparedStatement psCourse = connection.prepareStatement("SELECT * FROM Courses c WHERE c.SubjectCode = ? AND c.CourseNumber = ? AND c.EffectiveTermCode = ?;");
            psCourse.setString(1, subjectCode);
            psCourse.setString(2, courseNumber);
            psCourse.setString(3, effectiveTermCode);

            ResultSet rsCourse = psCourse.executeQuery();
            if (rsCourse.first()) {
                Float unitsHigh = rsCourse.getFloat("UnitsHigh");
                Float unitsLow = rsCourse.getFloat("UnitsLow");

                PreparedStatement psUpdateSectionGroupCost = connection.prepareStatement(
                    " UPDATE SectionGroupCosts" +
                        " SET UnitsHigh = ?," +
                        " UnitsLow = ?" +
                        " WHERE Id = ?;"
                );

                psUpdateSectionGroupCost.setFloat(1, unitsHigh);
                psUpdateSectionGroupCost.setFloat(2, unitsLow);
                psUpdateSectionGroupCost.setLong(3, sectionGroupCostId);
                psUpdateSectionGroupCost.execute();
                psUpdateSectionGroupCost.close();
            }
        }

        // Commit changes
        connection.commit();
    }
}
