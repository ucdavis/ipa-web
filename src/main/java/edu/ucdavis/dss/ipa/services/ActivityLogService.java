package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.validation.Loggable;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.util.List;

@Validated
public interface ActivityLogService {
    /**
     * Log the activity of a user in the database
     *
     * @param user    - The author of the log
     * @param message - The activity to log e.g. "Logged in"
     * @return - returns an ActivityLog with an id
     */
    ActivityLog logEntry(User user, String message);

    /**
     * Log the activity of a user in the database
     *
     * @param user    - The author of the log
     * @param entity  - An entity related to the action but not the author
     * @param message - The activity to log e.g. "Logged in"
     */
    void logEntry(User user, Loggable entity, String message);

    /**
     * Log the activity of a user in the database
     *
     * @param user    - The author of the log
     * @param entity  - A list of entities related to the action but not the author
     * @param message - The activity to log e.g. "Logged in"
     */
    void logEntry(User user, List<Loggable> entity, String message);

    /**
     * Returns a List of ActivityLogs linked with the specified user
     *
     * @param user
     * @return - A list of ActivityLogs with the given user Id
     */
    List<ActivityLog> findByUser(User user);

    /**
     * Returns an activity log with the given id
     *
     * @param id - id of ActivityLog
     * @return - An ActivityLog with the given id
     */
    ActivityLog findById(long id);

    /**
     * Returns a List of ActivityLogs after a given timestamp
     *
     * @param timestamp - Specified time to look after
     * @return - A list of ActivityLogs posted after the given timestamp
     */
    List<ActivityLog> findByTimestampAfter(Timestamp timestamp);

    /**
     * Returns a List of ActivityLogs before a given timestamp
     *
     * @param timestamp - Specified time to look before
     * @return - A list of ActivityLogs posted before the given timestamp
     */
    List<ActivityLog> findByTimestampBefore(Timestamp timestamp);

    /**
     * Returns a List of ActivityLogs at a given timestamp
     *
     * @param timestamp - Specified time to look for
     * @return - A list of ActivityLogs posted at the given timestamp
     */
    List<ActivityLog> findByTimestamp(Timestamp timestamp);
}
