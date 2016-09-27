package edu.ucdavis.dss.ipa.api.components.activitylog.views;

import edu.ucdavis.dss.ipa.entities.ActivityLog;

import java.util.ArrayList;
import java.util.List;

/**
 * The object to be serialized as a JSON object when /api/activityLog is called
 */
public class ActivityLogView {
    private List<ActivityLog> activityLogs = new ArrayList<>();

    public ActivityLogView(List<ActivityLog> activityLogs) {
        setActivityLogs(activityLogs);
    }

    public List<ActivityLog> getActivityLogs() { return this.activityLogs; }
    public void setActivityLogs(List<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
    }
}
