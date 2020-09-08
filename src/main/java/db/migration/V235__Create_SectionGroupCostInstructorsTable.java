package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V235__Create_SectionGroupCostInstructorsTable implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        // Create Section Group Costs Instructors Table
        PreparedStatement psCreateSectionGroupCostInstructorsTable =
                connection.prepareStatement(
                        "CREATE TABLE `SectionGroupCostInstructors`(" +
                                "  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
                                "  `InstructorId` int(11)," +
                                "  `OriginalInstructorId` int(11)," +
                                "  `InstructorTypeId` int(11)," +
                                "  `SectionGroupCostId` int(11) NOT NULL," +
                                "  `TeachingAssignmentId` int(11)," +
                                "  `Cost` decimal(15,2)," +
                                "  `Reason` varchar(30)," +
                                "  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                                "  `UpdatedAt` timestamp NULL DEFAULT NULL," +
                                "  `ModifiedBy` varchar(16) DEFAULT NULL" +
                                ");");
        psCreateSectionGroupCostInstructorsTable.execute();

        // Get SectionGroupCosts
        PreparedStatement psSectionGroupCostsQuery = connection.prepareStatement(
                "SELECT Id, InstructorId, InstructorTypeId, Cost, Reason FROM SectionGroupCosts; ");

        ResultSet rsSectionGroupCostsQuery = psSectionGroupCostsQuery.executeQuery();
        while(rsSectionGroupCostsQuery.next()){
            long sectionGroupCostId = rsSectionGroupCostsQuery.getLong("Id");
            Long instructorId = rsSectionGroupCostsQuery.getLong("InstructorId");
            Long instructorTypeId = rsSectionGroupCostsQuery.getLong("InstructorTypeId");
            BigDecimal cost = rsSectionGroupCostsQuery.getBigDecimal("Cost");
            String reason = rsSectionGroupCostsQuery.getString("Reason");

            PreparedStatement psCreateSectionGroupCostInstructor = connection.prepareStatement("INSERT INTO SectionGroupCostInstructors (SectionGroupCostId, InstructorId, InstructorTypeId, Cost, Reason) VALUES (?, ?, ?, ?, ?);");
            psCreateSectionGroupCostInstructor.setLong(1, sectionGroupCostId);
            psCreateSectionGroupCostInstructor.setLong(2, instructorId);
            psCreateSectionGroupCostInstructor.setLong(3, instructorTypeId);
            psCreateSectionGroupCostInstructor.setBigDecimal(4, cost);
            psCreateSectionGroupCostInstructor.setString(5, reason);
            psCreateSectionGroupCostInstructor.execute();
            psCreateSectionGroupCostInstructor.close();

        }
        rsSectionGroupCostsQuery.close();
    }

}

