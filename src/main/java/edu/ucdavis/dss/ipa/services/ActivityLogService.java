package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.User;
import org.springframework.validation.annotation.Validated;

import java.sql.Timestamp;
import java.util.List;

@Validated
public interface ActivityLogService {
    /**
     * Log the activity of a user in the database
     *
     * @param user    - The user engaging in the activity
     * @param message - The activity to log e.g. "Logged in"
     */
    void logEntry(User user, String message);

    /**
     * Returns a List of ActivityLogs linked with the specified user
     *
     * @param uid - id of user
     * @return - A list of ActivityLogs with the given user Id
     */
    List<ActivityLog> findByUserId(long uid);

    /**
     * Returns a List of ActivityLogs linked with the specified id
     *
     * @param id - id of ActivityLog
     * @return - A list of ActivityLogs with the given id
     */
    List<ActivityLog> findById(long id);

    /**
     * Returns a List of ActivityLogs after a given timestamp
     *
     * @param timestamp - Specified time to look after
     * @return - A list of ActivityLogs posted after the given timestamp
     */
    List<ActivityLog> findAfterTimestamp(Timestamp timestamp);

    /**
     * Returns a List of ActivityLogs before a given timestamp
     *
     * @param timestamp - Specified time to look before
     * @return - A list of ActivityLogs posted before the given timestamp
     */
    List<ActivityLog> findBeforeTimestamp(Timestamp timestamp);

    /**
     * Returns a List of ActivityLogs at a given timestamp
     *
     * @param timestamp - Specified time to look for
     * @return - A list of ActivityLogs posted at the given timestamp
     */
    List<ActivityLog> findByTimestamp(Timestamp timestamp);
}
