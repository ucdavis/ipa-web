package edu.ucdavis.dss.ipa.api.components.activitylog.views.factories;


import edu.ucdavis.dss.ipa.api.components.activitylog.views.ActivityLogView;

public interface ActivityLogViewFactory {
    ActivityLogView createActivityLogView(String tag, long limit);
}
