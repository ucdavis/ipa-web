package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.ActivityLogTag;

import java.util.List;

/**
 * Created by MarkDiez on 9/23/16.
 */
public interface ActivityLogTagService {
    ActivityLogTag findById(long id);

    List<ActivityLogTag> findByActivityLogId(long activityLogId);

    List<ActivityLogTag> findByTag(String tag);

    /**
     *
     * @param activityLogId - Should be in the format <entity>_<id>
     * @param tag
     */
    void addActivityLogTag(long activityLogId, String tag);
}
