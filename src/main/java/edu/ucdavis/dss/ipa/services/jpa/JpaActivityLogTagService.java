package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
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
    public void addActivityLogTag(long activityLogId, String tag) {
        ActivityLogTag logTag = new ActivityLogTag();
        logTag.setActivityLogId(activityLogId);
        logTag.setTag(tag);

        activityLogTagRepository.save(logTag);
    }

}
