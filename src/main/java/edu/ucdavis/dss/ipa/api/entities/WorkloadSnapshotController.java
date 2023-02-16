package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.entities.WorkloadSnapshot;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.services.WorkloadSnapshotService;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkloadSnapshotController {
    @Inject
    UserService userService;

    @Inject
    WorkgroupService workgroupService;

    @Inject
    Authorizer authorizer;

    @Inject
    Authorization authorization;

    @Inject
    WorkloadSnapshotService workloadSnapshotService;

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/workloadSnapshots", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<WorkloadSnapshot> getWorkloadSnapshots(@PathVariable long workgroupId, @PathVariable long year,
                                                       HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
        List<WorkloadSnapshot> workloadSnapshots = workloadSnapshotService.findByWorkgroupIdAndYear(workgroupId, year);

        return workloadSnapshots;
    }
}
