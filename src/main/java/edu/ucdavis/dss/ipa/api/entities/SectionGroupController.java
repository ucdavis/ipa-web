package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionGroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class SectionGroupController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject SectionGroupService sectionGroupService;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/sectionGroups", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<SectionGroup> getSectionGroups(@PathVariable long workgroupId,
                                             @PathVariable long year,
                                               HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findOrCreateByWorkgroupIdAndYear(workgroupId, year);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    return sectionGroupService.findByScheduleId(schedule.getId());
  }
}
