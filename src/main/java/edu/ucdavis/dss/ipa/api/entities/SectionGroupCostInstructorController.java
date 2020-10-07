package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.SectionGroupCostInstructor;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.SectionGroupCostInstructorService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class SectionGroupCostInstructorController {
    @Inject
    SectionGroupCostInstructorService sectionGroupCostInstructorService;
    @Inject
    WorkgroupService workgroupService;
    @Inject
    Authorizer authorizer;

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/sectionGroupCostInstructors", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public List<SectionGroupCostInstructor> getSectionGroupCosts(@PathVariable long workgroupId,
                                                       @PathVariable long year,
                                                       HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
        List<SectionGroupCostInstructor> sectionGroupCostInstructors = sectionGroupCostInstructorService.findbyWorkgroupIdAndYear(workgroupId, year);

        return sectionGroupCostInstructors;
    }
}
