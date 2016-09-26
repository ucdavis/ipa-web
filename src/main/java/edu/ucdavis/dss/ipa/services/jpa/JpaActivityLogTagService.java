package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
import edu.ucdavis.dss.ipa.entities.validation.Loggable;
import edu.ucdavis.dss.ipa.repositories.ActivityLogTagRepository;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;

import javax.inject.Inject;
import java.util.List;


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
    public void addActivityLogTag(ActivityLog activityLog, Loggable referenceEntity) {
        ActivityLogTag logTag = new ActivityLogTag();
        logTag.setActivityLog(activityLog);
        logTag.setTag(referenceEntity.getLogTag());

        activityLogTagRepository.save(logTag);
    }

}
