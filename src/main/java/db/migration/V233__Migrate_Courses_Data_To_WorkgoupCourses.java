package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V233__Migrate_Courses_Data_To_WorkgoupCourses implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {

        // Get TeachingCallReceipts comments
        PreparedStatement coursesQuery = connection.prepareStatement(
            "SELECT"
                    + " `Courses`.`Title`,"
                    + " `Courses`.`SubjectCode`,"
                    + " `Courses`.`CourseNumber`,"
                    + " `Courses`.`EffectiveTermCode`,"
                    + " `Workgroups`.`Id` AS `WorkgroupId`"
                    + " FROM `Courses`"
                    + " JOIN `Schedules`"
                    + " ON `Courses`.`ScheduleId`= `Schedules`.`Id`"
                    + " JOIN `Workgroups`"
                    + " ON `Workgroups`.`Id` = `Schedules`.`WorkgroupId`"
                    + " GROUP BY"
                    + " `Courses`.`Title`,"
                    + " `Courses`.`SubjectCode`,"
                    + " `Courses`.`CourseNumber`,"
                    + " `Courses`.`EffectiveTermCode`,"
                    + " `Workgroups`.`Id`;");

        ResultSet rsCourses = coursesQuery.executeQuery();

        try {
            // Loop over courses result and insert them into WorkgroupCourses
            while(rsCourses.next()) {
                Long workgroupId = rsCourses.getLong("workgroupId");
                String title = rsCourses.getString("title");
                String subjectCode = rsCourses.getString("subjectCode");
                String courseNumber = rsCourses.getString("courseNumber");
                String effectiveTermCode = rsCourses.getString("effectiveTermCode");

                String insertWorkgroupCoursesQuery = "INSERT INTO `WorkgroupCourses`"
                        + " (WorkgroupId, Title, SubjectCode, CourseNumber, EffectiveTermCode) "
                        + " VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertWorkgroupCoursesStatement = connection.prepareStatement(insertWorkgroupCoursesQuery);
                insertWorkgroupCoursesStatement.setLong(1, workgroupId);
                insertWorkgroupCoursesStatement.setString(2, title);
                insertWorkgroupCoursesStatement.setString(3, subjectCode);
                insertWorkgroupCoursesStatement.setString(4, courseNumber);
                insertWorkgroupCoursesStatement.setString(5, effectiveTermCode);

                insertWorkgroupCoursesStatement.execute();
                insertWorkgroupCoursesStatement.close();
            }

        } finally {
            rsCourses.close();
        }
    }

}
