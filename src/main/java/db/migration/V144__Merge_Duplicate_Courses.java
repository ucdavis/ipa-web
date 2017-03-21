package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class V144__Merge_Duplicate_Courses implements JdbcMigration {

    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psCourses = connection.prepareStatement("SELECT * FROM `Courses`");
        List<Long> courseIdsAlreadyProcessed = new ArrayList<>();

        try {
            connection.setAutoCommit(false);

            // Find Courses
            ResultSet rsCourses = psCourses.executeQuery();

            Integer numberDuplicateCoursesFound = 0;

            while (rsCourses.next()) {
                long courseId = rsCourses.getLong("Id");
                long scheduleId = rsCourses.getLong("ScheduleId");

                String subjectCode = rsCourses.getString("SubjectCode");
                String courseNumber = rsCourses.getString("CourseNumber");
                String sequencePattern = rsCourses.getString("SequencePattern");

                // Find Duplicate Courses
                PreparedStatement psDuplicateCourses = connection.prepareStatement(
                        "SELECT * FROM `Courses` WHERE `Courses`.`ScheduleId` = ? "
                                + " AND `Courses`.`SubjectCode` = ? "
                                + " AND `Courses`.`SequencePattern` = ? "
                                + " AND `Courses`.`CourseNumber` = ?;"
                );

                psDuplicateCourses.setLong(1, scheduleId);
                psDuplicateCourses.setString(2, subjectCode);
                psDuplicateCourses.setString(3, sequencePattern);
                psDuplicateCourses.setString(4, courseNumber);

                ResultSet rsDuplicateCourses = psDuplicateCourses.executeQuery();

                while (rsDuplicateCourses.next()) {
                    long slotCourseId = rsDuplicateCourses.getLong("Id");

                    // Ensure we are not comparing a course against itself, or looking at a course we already processed

                    if (slotCourseId != courseId && courseIdsAlreadyProcessed.indexOf(slotCourseId) == -1) {
                        numberDuplicateCoursesFound++;

                        // Find The Duplicate courses sectionGroups
                        PreparedStatement psDuplicateSectionGroups = connection.prepareStatement(
                                "SELECT * FROM `SectionGroups` WHERE `SectionGroups`.`CourseId` = ?;"
                        );

                        psDuplicateSectionGroups.setLong(1, slotCourseId);
                        ResultSet rsDuplicateSectionGroups = psDuplicateSectionGroups.executeQuery();

                        while (rsDuplicateSectionGroups.next()) {
                            Long slotSectionGroupId = rsDuplicateSectionGroups.getLong("Id");
                            String slotTermCode = rsDuplicateSectionGroups.getString("TermCode");

                            // Determine if course has an identical sectionGroup to the one on slotCourse
                            Long identicalOriginalSectionGroupId = null;

                            // Find The Course sectionGroups
                            PreparedStatement psSectionGroups = connection.prepareStatement(
                                    "SELECT * FROM `SectionGroups` WHERE `SectionGroups`.`CourseId` = ?;"
                            );

                            psSectionGroups.setLong(1, courseId);
                            ResultSet rsSectionGroups = psSectionGroups.executeQuery();

                            while (rsSectionGroups.next()) {
                                Long sectionGroupId = rsSectionGroups.getLong("Id");
                                String termCode = rsSectionGroups.getString("TermCode");

                                if (termCode.equals(slotTermCode)) {
                                    identicalOriginalSectionGroupId = sectionGroupId;
                                }
                            }
                            psSectionGroups.close();

                            // If this sectionGroup was not found on course, then point this slotSectionGroup at course
                            if (identicalOriginalSectionGroupId == null) {
                                PreparedStatement psSetCourse = connection.prepareStatement(
                                        " UPDATE `SectionGroups` SET `CourseId` = ? WHERE `Id` = ?;"
                                );

                                psSetCourse.setLong(1, courseId);
                                psSetCourse.setLong(2, slotSectionGroupId);

                                psSetCourse.execute();
                                psSetCourse.close();
                            } else {
                                // Attempt to point the slotSectionGroup sections to the course sectionGroup if they are unique


                                // Find The duplicate sections
                                PreparedStatement psDuplicateSections = connection.prepareStatement(
                                        "SELECT * FROM `Sections` WHERE `Sections`.`SectionGroupId` = ?;"
                                );

                                psDuplicateSections.setLong(1, slotSectionGroupId);
                                ResultSet rsDuplicateSections = psDuplicateSections.executeQuery();

                                while (rsDuplicateSections.next()) {
                                    Long slotSectionId = rsDuplicateSections.getLong("Id");
                                    String slotSectionSequencePattern = rsDuplicateSections.getString("SequencePattern");


                                    // Determine if sectionGroup has an identical section to the one on slotSectionGroup
                                    Long identicalOriginalSectionId = null;

                                    PreparedStatement psSections = connection.prepareStatement(
                                            "SELECT * FROM `Sections` WHERE `Sections`.`SectionGroupId` = ?;"
                                    );

                                    psSections.setLong(1, slotSectionGroupId);
                                    ResultSet rsSections = psSections.executeQuery();

                                    while (rsSections.next()) {
                                        Long sectionId = rsSections.getLong("Id");
                                        String sectionSequencePattern = rsSections.getString("SequencePattern");

                                        if (slotSectionSequencePattern.equals(sectionSequencePattern)) {
                                            identicalOriginalSectionId = sectionId;
                                        }
                                    }

                                    if (identicalOriginalSectionId == null) {
                                        PreparedStatement psSetCourse = connection.prepareStatement(
                                                " UPDATE `Sections` SET `SectionGroupId` = ? WHERE `Id` = ?;"
                                        );

                                        psSetCourse.setLong(1, identicalOriginalSectionGroupId);
                                        psSetCourse.setLong(2, slotSectionId);

                                        psSetCourse.execute();
                                        psSetCourse.close();
                                    }
                                }
                                psSectionGroups.close();
                            }
                        } // rsDuplicateSectionGroups

                        // Remove duplicate course
                        courseIdsAlreadyProcessed.add(slotCourseId);
                        courseIdsAlreadyProcessed.add(courseId);

                        PreparedStatement psDeleteCourse = connection.prepareStatement(
                            "DELETE FROM `Courses` WHERE `Id` = ?;"
                        );

                        psDeleteCourse.setLong(1, slotCourseId);

                        psDeleteCourse.execute();
                        psDeleteCourse.close();

                    } // Actual duplicate course found
                } // rsDuplicateCourses

                psDuplicateCourses.close();

            } // End rsCourses loop

            // Commit changes
            connection.commit();

        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (psCourses != null) {
                    psCourses.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}