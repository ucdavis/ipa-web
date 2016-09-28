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
}
