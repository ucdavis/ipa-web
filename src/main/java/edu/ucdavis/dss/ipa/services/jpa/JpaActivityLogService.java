package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.repositories.ActivityLogRepository;
import edu.ucdavis.dss.ipa.services.ActivityLogService;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by MarkDiez on 9/21/16.
 */
public class JpaActivityLogService implements ActivityLogService {
    @Inject ActivityLogRepository activityLogRepository;

    @Override
    public void logEntry(User user, String message) {
        ActivityLog newLog = new ActivityLog();
        newLog.setMessage(message);
        newLog.setUserId(user.getId());

        activityLogRepository.save(newLog);
    }

    @Override
    public List<ActivityLog> findByUserId(long uid) {
        return activityLogRepository.findByUserId(uid);
    }

    @Override
    public ActivityLog findById(long id) {
        return activityLogRepository.findByActvityLogId(id);
    }

    @Override
    public List<ActivityLog> findAfterTimestamp(Timestamp timestamp) {
        return activityLogRepository.findAfterTimestamp(timestamp);
    }

    @Override
    public List<ActivityLog> findBeforeTimestamp(Timestamp timestamp) {
        return activityLogRepository.findBeforeTimestamp(timestamp);
    }

    @Override
    public List<ActivityLog> findByTimestamp(Timestamp timestamp) {
        return activityLogRepository.findByTimestamp(timestamp);
    }
}
