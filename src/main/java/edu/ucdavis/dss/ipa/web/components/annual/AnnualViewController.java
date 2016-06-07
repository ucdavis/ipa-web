package edu.ucdavis.dss.ipa.web.components.annual;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;

import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.web.components.annual.views.AnnualView;
import edu.ucdavis.dss.ipa.web.components.annual.views.factories.AnnualViewFactory;

@RestController
public class AnnualViewController {
	@Inject ScheduleService scheduleService;
	@Inject AnnualViewFactory annualViewFactory;
	@Inject ScheduleTermStateService scheduleTermStateService;

	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator') or hasPermission(#id, 'schedule', 'senateInstructor') or hasPermission(#id, 'schedule', 'federationInstructor')")
	@RequestMapping(value = "/api/annualView/{id}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public AnnualView showAnnualView(@PathVariable long id, HttpServletResponse httpResponse) {
		Schedule schedule = this.scheduleService.findById(id);
		
		if (schedule == null) {
			httpResponse.setStatus(HttpStatus.BAD_REQUEST.value());
			return null;
		}

		return annualViewFactory.createAnnualScheduleView(schedule);
	}

	/**
	 * Exports a schedule with ID 'id' as an Excel .xls file
	 * 
	 * @param id
	 * @return
	 */
	@PreAuthorize("hasPermission(#id, 'schedule', 'academicCoordinator')")
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
