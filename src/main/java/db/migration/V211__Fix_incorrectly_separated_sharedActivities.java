package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;
import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class V211__Fix_incorrectly_separated_sharedActivities implements JdbcMigration {

    /**
     * Refactors repeated activities within a sectionGroup into a single shared activity
     * @param connection
     * @throws Exception
     */
    @Override
    public void migrate(Connection connection) throws Exception {
        // Look through all letter based sectionGroups
        // If each section has an activity that matches on day/start/end/type, delete those sections and create a sharedActivity with those options
        // use a hashmap of occurences, and a hashmap of matching activity ids
        // If activity count matches # of sections in sectionGroup, then delete all those sections and create a shared one

        PreparedStatement psSectionGroups = connection.prepareStatement("SELECT * FROM SectionGroups;");
        connection.setAutoCommit(false);

        ResultSet rsSectionGroups = psSectionGroups.executeQuery();

        while(rsSectionGroups.next()) {
            Long sectionGroupId = rsSectionGroups.getLong("Id");
            Long courseId = rsSectionGroups.getLong("CourseId");

            PreparedStatement psCourse = connection.prepareStatement("SELECT * FROM Courses c WHERE c.Id = ?;");
            psCourse.setLong(1, courseId);
            ResultSet rsCourse = psCourse.executeQuery();

            Boolean sectionGroupIsNumeric = true;

            if (rsCourse.first()) {
                String sequencePattern = rsCourse.getString("SequencePattern");
                if (sequencePattern != null && sequencePattern.length() > 0) {
                    sectionGroupIsNumeric = Character.isDigit(rsCourse.getString("SequencePattern").charAt(0));
                }
            }

            rsCourse.close();

            // SectionGroup is not letter based, so there is no opportunity to find repeated activities
            if (sectionGroupIsNumeric) { continue; }

            // Find sections for SectionGroup
            PreparedStatement psSections = connection.prepareStatement("SELECT * FROM Sections s WHERE s.SectionGroupId = ?;");
            psSections.setLong(1, sectionGroupId);
            ResultSet rsSections = psSections.executeQuery();

            Map<String, Long> activityOccurrences = new HashMap<String, Long>();
            Map<String, List<Long>> activityGroups = new HashMap<String, List<Long>>();
            List<String> activityKeys = new ArrayList<>();

            rsSections.last();
            int numSections = rsSections.getRow();
            rsSections.beforeFirst();

            while(rsSections.next()) {
                Long sectionId = rsSections.getLong("Id");

                PreparedStatement psActivities = connection.prepareStatement("SELECT * FROM Activities a WHERE a.SectionId = ?;");
                psActivities.setLong(1, sectionId);
                ResultSet rsActivities = psActivities.executeQuery();

                while(rsActivities.next()) {
                    Long activityId = rsActivities.getLong("Id");

                    String dayIndicator = rsActivities.getString("DayIndicator");
                    String typeCode = rsActivities.getString("ActivityTypeCode");
                    String startTime = rsActivities.getTime("StartTime") != null ? rsActivities.getTime("StartTime").toString() : "";
                    String endTime = rsActivities.getTime("EndTime") != null ? rsActivities.getTime("EndTime").toString() : "";

                    // Ignore activities that don't have days/times set
                    if (dayIndicator.indexOf("1") == -1 || startTime == "" || endTime == "") {
                        continue;
                    }

                    String activityKey = typeCode + startTime + endTime + dayIndicator;
                    activityKeys.add(activityKey);

                    Long activityCount = activityOccurrences.get(activityKey);
                    activityCount = activityCount != null ? activityCount : 0L;
                    activityCount += 1;
                    activityOccurrences.put(activityKey, activityCount);

                    List<Long> activityIds = activityGroups.get(activityKey);
                    activityIds = activityIds != null ? activityIds : new ArrayList<>();
                    activityIds.add(activityId);
                    activityGroups.put(activityKey, activityIds);
                }
            }

            for (String activityKey : activityKeys) {
                if (activityOccurrences.get(activityKey) != numSections) { continue; }

                Boolean foundExistingActivity = false;

                // Ensure a shared activity doesn't already exist matching this activityKey
                PreparedStatement psActivity = connection.prepareStatement("SELECT * FROM Activities a WHERE a.SectionGroupId = ?;");
                psActivity.setLong(1, sectionGroupId);
                ResultSet rsActivity = psActivity.executeQuery();

                while(rsActivity.next()) {
                    String dayIndicator = rsActivity.getString("DayIndicator");
                    String typeCode = rsActivity.getString("ActivityTypeCode");
                    String startTime = rsActivity.getTime("StartTime") != null ? rsActivity.getTime("StartTime").toString() : "";
                    String endTime = rsActivity.getTime("EndTime") != null ? rsActivity.getTime("EndTime").toString() : "";
                    String slotActivityKey = typeCode + startTime + endTime + dayIndicator;

                    if (activityKey.equals(slotActivityKey)) {
                        foundExistingActivity = true;
                    }
                }

                if (foundExistingActivity) { continue; }

                // Create shared activity
                PreparedStatement psActivityTemplate = connection.prepareStatement("SELECT * FROM Activities a WHERE a.Id = ?;");
                Long slotActivityId = activityGroups.get(activityKey).get(0);
                psActivityTemplate.setLong(1, slotActivityId);
                ResultSet rsActivityTemplate = psActivityTemplate.executeQuery();

                String activityTypeCode = null;
                String bannerLocation = null;
                Date beginDate = null;
                Date endDate = null;
                Time startTime = null;
                Time endTime = null;
                String dayIndicator = null;
                Long locationId = null;

                while (rsActivityTemplate.next()) {
                    activityTypeCode = rsActivityTemplate.getString("ActivityTypeCode");
                    bannerLocation = rsActivityTemplate.getString("BannerLocation");
                    beginDate = rsActivityTemplate.getDate("BeginDate");
                    endDate = rsActivityTemplate.getDate("EndDate");
                    startTime = rsActivityTemplate.getTime("StartTime");
                    endTime = rsActivityTemplate.getTime("EndTime");
                    dayIndicator = rsActivityTemplate.getString("DayIndicator") != null ? rsActivityTemplate.getString("DayIndicator") : "0000000";
                    locationId = rsActivityTemplate.getLong("LocationId");
                    locationId = rsActivityTemplate.wasNull() ? null : locationId;
                }

                PreparedStatement psCreateSharedActivity = connection.prepareStatement(
                    " INSERT INTO `Activities`" +
                    " (SectionGroupId, ActivityTypeCode, BannerLocation, BeginDate," +
                    " EndDate, StartTime, EndTime, DayIndicator, LocationId)" +
                    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);"
                );

                psCreateSharedActivity.setLong(1, sectionGroupId);
                psCreateSharedActivity.setString(2, activityTypeCode);
                psCreateSharedActivity.setString(3, bannerLocation);
                psCreateSharedActivity.setDate(4, beginDate);
                psCreateSharedActivity.setDate(5, endDate);
                psCreateSharedActivity.setTime(6, startTime);
                psCreateSharedActivity.setTime(7, endTime);
                psCreateSharedActivity.setString(8, dayIndicator);

                if (locationId == null) {
                    psCreateSharedActivity.setNull(9, java.sql.Types.INTEGER);
                } else {
                    psCreateSharedActivity.setLong(9, locationId);
                }

                psCreateSharedActivity.execute();

                // Delete activities in the group
                for (Long activityId : activityGroups.get(activityKey)) {
                    PreparedStatement psDeleteActivity = connection.prepareStatement(
                        "DELETE FROM `Activities` WHERE `Id` = ?;"
                    );

                    psDeleteActivity.setLong(1, activityId);
                    psDeleteActivity.execute();
                }
            }
        }

        // Commit changes
        connection.commit();
    }
}
