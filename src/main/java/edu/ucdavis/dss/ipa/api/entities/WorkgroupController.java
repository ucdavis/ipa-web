package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin
public class WorkgroupController {
    @Inject Authorizer authorizer;
    @Inject UserRoleService userRoleService;
    @Inject WorkgroupService workgroupService;

    @RequestMapping(value = "/api/workgroups", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public List<Workgroup> getWorkgroups(HttpServletResponse httpResponse) {
        authorizer.isAdmin();

        return workgroupService.findAll();
    }
}
