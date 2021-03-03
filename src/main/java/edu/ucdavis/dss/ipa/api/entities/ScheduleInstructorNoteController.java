package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.ScheduleInstructorNote;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ScheduleInstructorNoteService;
import edu.ucdavis.dss.ipa.services.ScheduleService;
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
public class ScheduleInstructorNoteController {
    @Inject
    ScheduleService scheduleService;
    @Inject
    ScheduleInstructorNoteService scheduleInstructorNoteService;
    @Inject
    Authorizer authorizer;

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/scheduleInstructorNotes", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public List<ScheduleInstructorNote> getScheduleInstructorNotes(@PathVariable long workgroupId,
                                                                   @PathVariable long year,
                                                                   HttpServletResponse httpResponse) {
        authorizer.isAuthorized();

        Schedule schedule = scheduleService.findByWorkgroupIdAndYear(workgroupId, year);

        if (schedule == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        return scheduleInstructorNoteService.findByScheduleId(schedule.getId());
    }
}
