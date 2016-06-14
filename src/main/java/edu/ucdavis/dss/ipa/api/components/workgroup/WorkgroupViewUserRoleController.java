package edu.ucdavis.dss.ipa.api.components.workgroup;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.ipa.api.views.UserViews;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewUserRoleController {

    @Inject WorkgroupService workgroupService;

    @PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/users", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(UserViews.Detailed.class)
    public List<User> getUserRolesByWorkgroupCode(@PathVariable String workgroupCode, HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);

        List<User> users = new ArrayList<User>();

        for (UserRole userRole : workgroup.getUserRoles()) {
            if (!users.contains(userRole.getUser())) {
                users.add(userRole.getUser());
            }
        }

        return users;
    }

}