package db.migration;

import org.apache.commons.lang3.math.NumberUtils;
import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by okadri on 6/16/16.
 */
public class V116__Split_Courses_With_More_Than_One_Sequence_Pattern implements JdbcMigration {

    public boolean isNumeric(String s) {
        return s.matches("[-+]?\\d*\\.?\\d+");
    }

    @Override
    public void migrate(Connection connection) throws Exception {
        ResultSet rsAllCourses = null;

        try {
//            connection.setAutoCommit(false);

            // Add required metadata columns to SectionGroups
            PreparedStatement psAddMetadataColumns = connection.prepareStatement(
                    " ALTER TABLE `Courses` " +
                            " ADD COLUMN `SequencePattern` VARCHAR(3) NOT NULL;"
            );
            psAddMetadataColumns.execute();

            // Make the Sequence Number not Null
            PreparedStatement psSequenceNumberNotNull = connection.prepareStatement(
                    " ALTER TABLE `Sections` " +
                            " CHANGE COLUMN `SequenceNumber` `SequenceNumber` VARCHAR(3) NOT NULL;"
            );
            psSequenceNumberNotNull.execute();

            // Find Orphaned teachingAssignments that aren't associated to a teachingPreference (Caused by initial historical schedule import)
            PreparedStatement psAllCourses = connection.prepareStatement(
                    " SELECT * FROM `Courses`;"
            );
            rsAllCourses = psAllCourses.executeQuery();

            while (rsAllCourses.next()) {
                long courseId = rsAllCourses.getLong("id");
                long scheduleId = rsAllCourses.getLong("ScheduleId");
                String title = rsAllCourses.getString("Title");
                String subjectCode = rsAllCourses.getString("SubjectCode");
                String courseNumber = rsAllCourses.getString("CourseNumber");
                String effectiveTermCode = rsAllCourses.getString("EffectiveTermCode");
                float unitsLow = rsAllCourses.getFloat("UnitsLow");
                float unitsHigh = rsAllCourses.getFloat("UnitsHigh");

                // This is used to track if the course has a pattern set later in the section loop
                boolean patternIsSet = false;
                Map<String, Long> sequenceCourseIdPairs = new HashMap<String, Long>();

                PreparedStatement psSectionGroups = connection.prepareStatement(
                        " SELECT * FROM `SectionGroups` " +
                                " WHERE `SectionGroups`.`CourseId` = ?; "
                );
                psSectionGroups.setLong(1, courseId);
                ResultSet rsSectionGroup = psSectionGroups.executeQuery();

                while (rsSectionGroup.next()) {
                    long sectionGroupId = rsSectionGroup.getLong("SectionGroupId");

                    PreparedStatement psSections = connection.prepareStatement(
                            " SELECT * FROM `Sections` " +
                                    " WHERE `Sections`.`SectionGroups_SectionGroupId` = ?; "
                    );
                    psSections.setLong(1, sectionGroupId);
                    ResultSet rsSections = psSections.executeQuery();

                    if (rsSections.next()) {
                        String sequenceNumber = rsSections.getString("SequenceNumber");
                        String key;

                        // If the sequenceNumber is numeric, then the key is the whole sequence
                        // Otherwise the key is the first letter
                        if (isNumeric(sequenceNumber)) {
                            key = sequenceNumber;
                        } else {
                            key = Character.toString(sequenceNumber.charAt(0));
                        }

                        // Set the pattern for the Course if it was not set already
                        if (!patternIsSet) {
                            PreparedStatement psSetMetaData = connection.prepareStatement(
                                    " UPDATE `Courses` c " +
                                            " SET c.`SequencePattern` = ? " +
                                            " WHERE c.`id` = ?; "
                            );
                            psSetMetaData.setString(1, key);
                            psSetMetaData.setLong(2, courseId);

                            psSetMetaData.execute();
                            psSetMetaData.close();

                            patternIsSet = true;
                            sequenceCourseIdPairs.put(key, courseId);
                        }

                        // if the key exists in the map then update the sectionGroup courseId
                        // Otherwise create a new Course and assign its id to the sectionGroup
                        long newCourseId = 0;
                        if (sequenceCourseIdPairs.get(key) != null) {
                            newCourseId = sequenceCourseIdPairs.get(key);
                        } else {
                            PreparedStatement psCreateCourse = connection.prepareStatement(
                                    "INSERT INTO `Courses` (ScheduleId, Title, UnitsLow, UnitsHigh, SubjectCode, " +
                                            "CourseNumber, EffectiveTermCode, SequencePattern) " +
                                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
                                    , Statement.RETURN_GENERATED_KEYS
                            );
                            psCreateCourse.setLong(1, scheduleId);
                            psCreateCourse.setString(2, title);
                            psCreateCourse.setFloat(3, unitsLow);
                            psCreateCourse.setFloat(4, unitsHigh);
                            psCreateCourse.setString(5, subjectCode);
                            psCreateCourse.setString(6, courseNumber);
                            psCreateCourse.setString(7, effectiveTermCode);
                            psCreateCourse.setString(8, key);
                            psCreateCourse.executeUpdate();

                            ResultSet rsCreateCourse = psCreateCourse.getGeneratedKeys();

                            if (rsCreateCourse.next()) {
                                newCourseId = rsCreateCourse.getLong(1);
                                sequenceCourseIdPairs.put(key, newCourseId);
                            }

                        }


                        if (newCourseId == 0) {
                            System.out.println("Course was not found nor created for sectionGroupIs" + sectionGroupId);
                        }

                        // Update SectionGroup CourseId to the newCourseId
                        PreparedStatement psSetMetaData = connection.prepareStatement(
                                " UPDATE `SectionGroups` sgs " +
                                        " SET sgs.`CourseId` = ? " +
                                        " WHERE sgs.`SectionGroupId` = ?; "
                        );
                        psSetMetaData.setLong(1, newCourseId);
                        psSetMetaData.setLong(2, sectionGroupId);
                        psSetMetaData.execute();
                        psSetMetaData.close();
                    }
                }

            }

//            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (rsAllCourses != null) {
                    rsAllCourses.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
