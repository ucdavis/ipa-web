package edu.ucdavis.dss.ipa.api.components.activitylog;

import edu.ucdavis.dss.ipa.api.components.activitylog.views.ActivityLogView;
import edu.ucdavis.dss.ipa.api.components.activitylog.views.factories.ActivityLogViewFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

@RestController
@CrossOrigin
public class ActivityLogViewController {
    @Inject ActivityLogViewFactory activityLogViewFactory;

    @RequestMapping(value = "/api/activityLog", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public ActivityLogView getActivityLogView(@RequestParam(value="search") String tag, @RequestParam(value="limit") long limit) {
        System.out.println("Attempted to create an activity view with tag: " + tag + " and limit " + limit);
        return activityLogViewFactory.createActivityLogView(tag, limit);
    }
}
