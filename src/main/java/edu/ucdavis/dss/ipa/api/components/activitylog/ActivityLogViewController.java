package edu.ucdavis.dss.ipa.api.components.activitylog;

import edu.ucdavis.dss.ipa.api.components.activitylog.views.ActivityLogView;
import edu.ucdavis.dss.ipa.api.components.activitylog.views.factories.ActivityLogViewFactory;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class ActivityLogViewController {
    @Inject ActivityLogViewFactory activityLogViewFactory;

    @RequestMapping(value = "/api/activityLogView?search={tag}&limit={limit}", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public ActivityLogView getActivityLogView(@PathVariable String tag, @PathVariable long limit) {
        return activityLogViewFactory.createActivityLogView(tag, limit);
    }
}
