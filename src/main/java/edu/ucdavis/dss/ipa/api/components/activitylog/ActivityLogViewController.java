package edu.ucdavis.dss.ipa.api.components.activitylog;

import edu.ucdavis.dss.ipa.api.components.activitylog.views.ActivityLogView;
import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.ActivityLogTag;
import edu.ucdavis.dss.ipa.services.ActivityLogService;
import edu.ucdavis.dss.ipa.services.ActivityLogTagService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin
public class ActivityLogViewController {
    @Inject ActivityLogService activityLogService;
    @Inject ActivityLogTagService activityLogTagService;

    /**
     * Retrieves a List of activity logs related to the tag sent
     * @param tag - [entity]_[id]
     * @param limit - maximum number of activity logs to return
     * @return - A JSON object filled with activity logs
     */
    @RequestMapping(value = "/api/activityLog", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public ActivityLogView getActivityLogView(@RequestParam(value="search") String tag, @RequestParam(value="limit") long limit) {
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
