package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
import edu.ucdavis.dss.ipa.repositories.ActivityLogTagRepository;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Service
public class JpaActivityLogTagService implements ActivityLogTagService {
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
        String loggableTag = "";

        try {
            String entityClass = referenceEntity.getClass().getSimpleName().toLowerCase();
            Method getEntityId = referenceEntity.getClass().getMethod("getId", null);
            long entityId = (long) getEntityId.invoke(referenceEntity, null);

            loggableTag = entityClass + "_" + entityId;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        logTag.setTag(loggableTag);
        activityLogTagRepository.save(logTag);
    }

}
