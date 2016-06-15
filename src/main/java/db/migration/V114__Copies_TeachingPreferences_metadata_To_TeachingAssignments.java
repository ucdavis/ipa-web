package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Lloyd on 6/15/16.
 */

public class V114__Copies_TeachingPreferences_metadata_To_TeachingAssignments implements JdbcMigration {

    @Override
    public void migrate(Connection connection) {
        ResultSet rsAllTeachingAssignments = null;

        try {
            connection.setAutoCommit(false);

            // Add required metadata columns to SectionGroups
            PreparedStatement psAddMetadataColumns = connection.prepareStatement(
                    " ALTER TABLE `TeachingAssignments`" +
                            " ADD COLUMN `TermCode` VARCHAR(6) NOT NULL," +
                            " ADD COLUMN `Priority` INT(11) NOT NULL," +
                            " ADD COLUMN `Buyout` TINYINT(1) NOT NULL DEFAULT 0," +
                            " ADD COLUMN `CourseRelease` TINYINT(1) NOT NULL DEFAULT 0," +
                            " ADD COLUMN `Sabbatical` TINYINT(1) NOT NULL DEFAULT 0," +
                            " ADD COLUMN `ScheduleId` INT(11) NOT NULL," +
                            " ADD COLUMN `TeachingPreferenceId` INT(11) NOT NULL," +
                            " ADD COLUMN `Approved` TINYINT(1) NOT NULL DEFAULT 0;"
            );

            // Find Orphaned teachingAssignments that aren't associated to a teachingPreference (Caused by initial historical schedule import)
            PreparedStatement psAllTeachingAssignments = connection.prepareStatement(
                    " SELECT * FROM `TeachingAssignments`;"
            );

            rsAllTeachingAssignments = psAllTeachingAssignments.executeQuery();

            psAddMetadataColumns.execute();

            while (rsAllTeachingAssignments.next()) {
                long sectionGroupId = rsAllTeachingAssignments.getLong("SectionGroups_SectionGroupId");
                long teachingAssignmentId = rsAllTeachingAssignments.getLong("TeachingAssignmentId");
                PreparedStatement psSectionGroup = connection.prepareStatement(
                        " SELECT * FROM `SectionGroups` " +
                                " WHERE `SectionGroups`.`SectionGroupId` = ?; "
                );
                psSectionGroup.setLong(1, sectionGroupId);
                ResultSet rsSectionGroup = psSectionGroup.executeQuery();

                if (rsSectionGroup.next()) {
                    String termCode = rsSectionGroup.getString("TermCode");

                    long courseOfferingId = rsSectionGroup.getLong("CourseOfferings_CourseOfferingId");
                    PreparedStatement psCourseOffering = connection.prepareStatement(
                            " SELECT * FROM `CourseOfferings` " +
                                    " WHERE `CourseOfferings`.`CourseOfferingId` = ?; "
                    );
                    psCourseOffering.setLong(1, courseOfferingId);
                    ResultSet rsCourseOffering = psCourseOffering.executeQuery();

                    if (rsCourseOffering.next()) {
                        long courseOfferingGroupId = rsCourseOffering.getLong("CourseOfferingGroupId");
                        PreparedStatement psCourseOfferingGroup = connection.prepareStatement(
                                " SELECT * FROM `CourseOfferingGroups` " +
                                        " WHERE `CourseOfferingGroups`.`id` = ?; "
                        );
                        psCourseOfferingGroup.setLong(1, courseOfferingGroupId);
                        ResultSet rsCourseOfferingGroup = psCourseOfferingGroup.executeQuery();

                        if (rsCourseOfferingGroup.next()) {
                            long scheduleId = rsCourseOfferingGroup.getLong("ScheduleId");

                            // Set TeachingAssignment metadata on TeachingAssignment
                            PreparedStatement psSetMetaData = connection.prepareStatement(
                                    " UPDATE `TeachingAssignments` tas " +
                                            " SET tas.`termCode` = ?, " +
                                            "     tas.`scheduleId` = ?, " +
                                            "     tas.`approved` = 1 " +
                                            " WHERE tas.`TeachingAssignmentId` = ?; "
                            );
                            psSetMetaData.setString(1, termCode);
                            psSetMetaData.setLong(2, scheduleId);
                            psSetMetaData.setLong(3, teachingAssignmentId);
                            psSetMetaData.execute();
                            psSetMetaData.close();
                        }
                    }
                }
            }
        } catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (rsAllTeachingAssignments != null) {
                    rsAllTeachingAssignments.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Get TeachingPreferences
        // For each TeachingPreference, is there the correct number of teachingAssignments
        // If not, make them.
        // Update existing teachingAssignments

        ResultSet rsTps = null;

        try {
            // Get all the SectionGroups
            PreparedStatement psTps = connection.prepareStatement(
                    "SELECT * FROM `TeachingPreferences`");
            rsTps = psTps.executeQuery();

            // Loop over TeachingPreferences
            while(rsTps.next()) {
                long courseOfferingId = rsTps.getLong("CourseOfferings_CourseOfferingId");
                long instructorId = rsTps.getLong("Instructors_InstructorId");
                String termCode = rsTps.getString("TermCode");
                long priority = rsTps.getLong("Priority");
                long buyout = rsTps.getLong(("isBuyout"));
                long courseRelease = rsTps.getLong("isCourseRelease");
                long sabbatical = rsTps.getLong("isSabbatical");
                long scheduleId = rsTps.getLong("Schedules_ScheduleId");
                long approved = rsTps.getLong("Approved");
                long teachingPreferenceId = rsTps.getLong("TeachingPreferenceId");

                // Look for associated SectionGroups to CourseOfferingId
                PreparedStatement psSectionGroups = connection.prepareStatement(
                        " SELECT * FROM `SectionGroups` " +
                                " WHERE `SectionGroups`.`CourseOfferings_CourseOfferingId` = ?; "
                );
                psSectionGroups.setLong(1, courseOfferingId);
                ResultSet rsSectionGroups = psSectionGroups.executeQuery();

                while (rsSectionGroups.next()) {

                    long sectionGroupId = rsSectionGroups.getLong("SectionGroupId");

                    PreparedStatement psTeachingAssignments = connection.prepareStatement(
                            " SELECT * FROM `TeachingAssignments` " +
                                    " WHERE `TeachingAssignments`.`SectionGroups_SectionGroupId` = ? " +
                                    " AND `TeachingAssignments`.`Instructors_InstructorId` = ?;"

                    );
                    psTeachingAssignments.setLong(1, sectionGroupId);
                    psTeachingAssignments.setLong(2, instructorId);

                    ResultSet rsTeachingAssignments = psTeachingAssignments.executeQuery();

                    // If a teachingAssignment is returned update its values
                    if (rsTeachingAssignments.next()) {
                        long teachingAssignmentId = rsTeachingAssignments.getLong("TeachingAssignmentId");

                        // Set CourseOffering metadata on SectionGroup
                        PreparedStatement psSetMetaData = connection.prepareStatement(
                                " UPDATE `TeachingAssignments` tas " +
                                        " SET tas.`priority` = ?, " +
                                        "     tas.`termCode` = ?, " +
                                        "     tas.`buyout` = ?, " +
                                        "     tas.`courseRelease` = ?, " +
                                        "     tas.`sabbatical` = ?, " +
                                        "     tas.`scheduleId` = ?, " +
                                        "     tas.`approved` = ? " +
                                        " WHERE tas.`TeachingAssignmentId` = ?; "
                        );
                        psSetMetaData.setLong(1, priority);
                        psSetMetaData.setString(2, termCode);
                        psSetMetaData.setLong(3, buyout);
                        psSetMetaData.setLong(4, courseRelease);
                        psSetMetaData.setLong(5, sabbatical);
                        psSetMetaData.setLong(6, scheduleId);
                        psSetMetaData.setLong(7, approved);
                        psSetMetaData.setLong(8, teachingAssignmentId);

                        psSetMetaData.execute();
                        psSetMetaData.close();

                    } else {

                        // Need to create an assignment
                        PreparedStatement psCreateAssignment = connection.prepareStatement(
                                "INSERT INTO `TeachingAssignments` (priority, termCode, buyout, courseRelease," +
                                        "sabbatical, scheduleId, approved, Instructors_InstructorId, SectionGroups_SectionGroupId, TeachingPreferenceId) " +
                                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
                        );

                        psCreateAssignment.setLong(1, priority);
                        psCreateAssignment.setString(2, termCode);
                        psCreateAssignment.setLong(3, buyout);
                        psCreateAssignment.setLong(4, courseRelease);
                        psCreateAssignment.setLong(5, sabbatical);
                        psCreateAssignment.setLong(6, scheduleId);
                        psCreateAssignment.setLong(7, approved);
                        psCreateAssignment.setLong(8, instructorId);
                        psCreateAssignment.setLong(9, sectionGroupId);
                        psCreateAssignment.setLong(10, teachingPreferenceId);

                        psCreateAssignment.execute();
                        psCreateAssignment.close();
                    }
                }
            }
        }  catch(SQLException e) {
            e.printStackTrace();
            return;
        } finally {
            try {
                if (rsTps != null) {
                    rsTps.close();
                }

                connection.commit();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
