package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.User;
//import edu.ucdavis.dss.ipa.repositories.ActivityLogRepository;
import edu.ucdavis.dss.ipa.repositories.UserRepository;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.ActivityLogService;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.List;

//@Service
public class JpaActivityLogService implements ActivityLogService {
    //@Inject ActivityLogRepository activityLogRepository;
    @Inject UserRepository userRepository;

    @Override
    public ActivityLog logEntry(String message) {
        User currentUser = userRepository.findByLoginId(Authorization.getLoginId());

        return this.logEntry(currentUser, message);
    }

    @Override
    public ActivityLog logEntry(User user, String message) {
//        ActivityLog newLog = new ActivityLog();
//        newLog.setMessage(message);
//        newLog.setUser(user);
//
//        return activityLogRepository.save(newLog);
        return null;
    }

    @Override
    public void logEntry(User user, Object entity, String message) {
        // Save the log and get an ActivityLog with an id
        ActivityLog activityLog = logEntry(user, message);

        // This if statement is necessary because a List<Object> is an Object
        // Attempting to call an overloaded function with Object<List> would navigate here instead.
        if (entity instanceof List<?>) {
            for (Object e : (List<Object>)entity) {
                activityLog.addTag(e);
            }
        } else {
            activityLog.addTag(entity);
        }

        //activityLogRepository.save(activityLog);
    }

    @Override
    public List<ActivityLog> findByUser(User user) {
        return null; //return activityLogRepository.findByUser(user);
    }

    @Override
    public ActivityLog findById(long id) {
        return null; //return activityLogRepository.findById(id);
    }

    @Override
    public List<ActivityLog> findByTimestampAfter(Timestamp timestamp) {
        return null; //return activityLogRepository.findByTimestampGreaterThan(timestamp);
    }

    @Override
    public List<ActivityLog> findByTimestampBefore(Timestamp timestamp) {
        return null; //return activityLogRepository.findByTimestampLessThan(timestamp);
    }

    @Override
    public List<ActivityLog> findByTimestamp(Timestamp timestamp) {
        return null; //return activityLogRepository.findByTimestamp(timestamp);
    }
}
