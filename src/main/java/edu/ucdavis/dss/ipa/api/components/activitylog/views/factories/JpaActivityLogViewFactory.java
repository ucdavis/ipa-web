package edu.ucdavis.dss.ipa.api.components.activitylog.views.factories;

import edu.ucdavis.dss.ipa.api.components.activitylog.views.ActivityLogView;
import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
import edu.ucdavis.dss.ipa.services.ActivityLogService;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaActivityLogViewFactory implements ActivityLogViewFactory{
    @Inject ActivityLogService activityLogService;
    @Inject ActivityLogTagService activityLogTagService;

    @Override
    public ActivityLogView createActivityLogView(String tag, long limit) {
        List<ActivityLog> relevantLogs = new ArrayList<>();

        int index = 0;
        for(ActivityLogTag alt : activityLogTagService.findByTag(tag)) {
            relevantLogs.add(activityLogService.findById(alt.getId()));
            index++;
            if (index >= limit) break;
        }

        return new ActivityLogView(relevantLogs);
    }
}
