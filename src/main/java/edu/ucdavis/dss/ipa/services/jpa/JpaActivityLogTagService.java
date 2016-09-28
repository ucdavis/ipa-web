package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
import edu.ucdavis.dss.ipa.repositories.ActivityLogTagRepository;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Service
public class JpaActivityLogTagService implements ActivityLogTagService {
    private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

    @Inject ActivityLogTagRepository activityLogTagRepository;

    @Override
    public ActivityLogTag findById(long id) {
        return activityLogTagRepository.findById(id);
    }

    @Override
    public List<ActivityLogTag> findByActivityLogId(long activityLogId) {
        return activityLogTagRepository.findByActivityLogId(activityLogId);
    }

    @Override
    public List<ActivityLogTag> findByTag(String tag) {
        return activityLogTagRepository.findByTag(tag);
    }

    @Override
    public void addActivityLogTag(ActivityLog activityLog, Object referenceEntity) {
        ActivityLogTag logTag = new ActivityLogTag();
        logTag.setActivityLog(activityLog);

        // Initialize tag with the name of the entity
        String loggableTag = referenceEntity.getClass().getSimpleName().toLowerCase();

        try {
            Method getEntityId = referenceEntity.getClass().getMethod("getId", null);
            long entityId = (long) getEntityId.invoke(referenceEntity, null);

            loggableTag += "_" + entityId;
        } catch (InvocationTargetException e) {
            log.debug("Invocation Target Exception when trying to get method getId on " + loggableTag);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.debug("Illegal Access Exception when trying to get method getId on " + loggableTag);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            log.debug("Could not find method getId for " + loggableTag);
            e.printStackTrace();
        }

        logTag.setTag(loggableTag);
        activityLogTagRepository.save(logTag);
    }

}
