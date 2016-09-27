package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ActivityLogTagService {
    ActivityLogTag findById(long id);

    List<ActivityLogTag> findByActivityLogId(long activityLogId);

    List<ActivityLogTag> findByTag(String tag);

    /**
     *
     * @param activityLog - Should be in the format <entity>_<id>
     * @param referenceEntity - The entity related to an ActivityLog
     */
    void addActivityLogTag(ActivityLog activityLog, Object referenceEntity);
}
