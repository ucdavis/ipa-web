package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

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
                "SELECT ta.InstructorId as TeachingAssingmentInstructorId, ta.Id as TeachingAssignmentId, bs.FromLiveData AS FromLiveData, sgc.Id AS SectionGroupCostId, sgc.InstructorId AS InstructorId, sgc.InstructorTypeId AS InstructorTypeId, sgc.Cost AS Cost, sgc.Reason AS Reason FROM SectionGroupCosts AS sgc LEFT JOIN BudgetScenarios AS bs ON bs.Id = sgc.BudgetScenarioId LEFT JOIN Budgets as b on bs.BudgetId = b.Id LEFT JOIN Schedules as s on b.ScheduleId = s.Id LEFT JOIN Courses as c on c.ScheduleId = s.Id AND c.SequencePattern = sgc.SequencePattern AND c.CourseNumber = sgc.CourseNumber AND c.SubjectCode = sgc.SubjectCode LEFT JOIN SectionGroups AS sg ON sg.CourseId = c.Id AND sg.TermCode = sgc.TermCode LEFT JOIN TeachingAssignments AS ta ON ta.Id = (SELECT tas.Id FROM TeachingAssignments as tas WHERE tas.SectionGroupId=sg.Id AND (tas.InstructorId = sgc.InstructorId or tas.InstructorTypeId = sgc.InstructorTypeId) ORDER BY tas.InstructorId DESC LIMIT 1) WHERE sgc.InstructorId IS NOT NULL OR sgc.InstructorTypeId IS NOT NULL ORDER BY sgc.id;  ");

        ResultSet rsSectionGroupCostsQuery = psSectionGroupCostsQuery.executeQuery();
        while(rsSectionGroupCostsQuery.next()){
            long sectionGroupCostId = rsSectionGroupCostsQuery.getLong("SectionGroupCostId");
            Long instructorId = rsSectionGroupCostsQuery.getLong("InstructorId");
            Long instructorTypeId = rsSectionGroupCostsQuery.getLong("InstructorTypeId");
            BigDecimal cost = rsSectionGroupCostsQuery.getBigDecimal("Cost");
            String reason = rsSectionGroupCostsQuery.getString("Reason");
            Integer fromLiveData = rsSectionGroupCostsQuery.getInt("FromLiveData");
            Long teachingAssignmentId = rsSectionGroupCostsQuery.getLong("TeachingAssignmentId");

            if(instructorId != 0 || instructorTypeId != 0){
                PreparedStatement psCreateSectionGroupCostInstructor = connection.prepareStatement("INSERT INTO SectionGroupCostInstructors (SectionGroupCostId, InstructorId, InstructorTypeId, Cost, Reason, TeachingAssignmentId) VALUES (?, ?, ?, ?, ?, ?);");
                psCreateSectionGroupCostInstructor.setLong(1, sectionGroupCostId);
                if(instructorId != 0){
                    psCreateSectionGroupCostInstructor.setLong(2, instructorId);
                } else{
                    psCreateSectionGroupCostInstructor.setNull(2, Types.INTEGER);
                }
                if(instructorTypeId != 0){
                    psCreateSectionGroupCostInstructor.setLong(3, instructorTypeId);
                } else{
                    psCreateSectionGroupCostInstructor.setNull(3, Types.INTEGER);
                }
                if(cost != BigDecimal.ZERO){
                    psCreateSectionGroupCostInstructor.setBigDecimal(4, cost);
                } else{
                    psCreateSectionGroupCostInstructor.setNull(4, Types.FLOAT);
                }
                psCreateSectionGroupCostInstructor.setString(5, reason);
                if(fromLiveData == 1 && teachingAssignmentId != 0){
                    psCreateSectionGroupCostInstructor.setLong(6, teachingAssignmentId);
                } else {
                    psCreateSectionGroupCostInstructor.setNull(6, Types.INTEGER);
                }
                psCreateSectionGroupCostInstructor.execute();
                psCreateSectionGroupCostInstructor.close();
            }

        }
        rsSectionGroupCostsQuery.close();
    }

}

