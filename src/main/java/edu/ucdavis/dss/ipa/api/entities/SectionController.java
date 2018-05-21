package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.Section;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin
public class SectionController {
  @Inject ScheduleService scheduleService;
  @Inject Authorizer authorizer;
  @Inject SectionService sectionService;

  @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/sections", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<Section> getSectionGroups(@PathVariable long workgroupId,
                                        @PathVariable long year,
                                        HttpServletResponse httpResponse) {
    Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

    if (schedule == null) {
      httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
      return null;
    }

    authorizer.hasWorkgroupRoles(schedule.getWorkgroup().getId(), "academicPlanner", "reviewer", "instructor", "studentPhd", "studentMasters", "instructionalSupport");

    return sectionService.findVisibleByWorkgroupIdAndYear(workgroupId, year);
  }
}
