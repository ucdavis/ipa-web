package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
//import edu.ucdavis.dss.ipa.repositories.ActivityLogTagRepository;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;

import java.util.List;

//@Service
public class JpaActivityLogTagService implements ActivityLogTagService {
    //@Inject ActivityLogTagRepository activityLogTagRepository;

    @Override
    public ActivityLogTag findById(long id) {
        return null; //return activityLogTagRepository.findById(id);
    }

    @Override
    public List<ActivityLogTag> findByActivityLogId(long activityLogId) {
        return null; //return activityLogTagRepository.findByActivityLogId(activityLogId);
    }

    @Override
    public List<ActivityLogTag> findByTag(String tag) {
        return null; //return activityLogTagRepository.findByTag(tag);
    }

}
