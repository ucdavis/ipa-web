package edu.ucdavis.dss.ipa.api.components.course;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.entities.Course;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.api.components.course.views.AnnualView;
import edu.ucdavis.dss.ipa.api.components.course.views.factories.AnnualViewFactory;

import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class CourseViewController {
	@Inject ScheduleService scheduleService;
	@Inject WorkgroupService workgroupService;
	@Inject AnnualViewFactory annualViewFactory;
	@Inject ScheduleTermStateService scheduleTermStateService;

	@RequestMapping(value = "/api/courseView/workgroups/{workgroupId}/years/{year}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Course> showAnnualView(@PathVariable long workgroupId, @PathVariable long year, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

		Workgroup workgroup = this.workgroupService.findOneById(workgroupId);
		Schedule schedule = this.scheduleService.findByWorkgroupAndYear(workgroup, year);
		
		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return schedule.getCourses();
	}

	/**
	 * Exports a schedule with ID 'id' as an Excel .xls file
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/api/annualView/{id}/excel")
	public View excelExport(@PathVariable long id, HttpServletResponse httpResponse) {
		Schedule schedule = this.scheduleService.findById(id);
		
		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return annualViewFactory.createAnnualScheduleExcelView(annualViewFactory.createAnnualScheduleView(schedule));
	}
}
