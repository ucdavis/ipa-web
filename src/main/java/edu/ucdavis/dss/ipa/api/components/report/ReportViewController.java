package edu.ucdavis.dss.ipa.api.components.report;

import edu.ucdavis.dss.ipa.api.components.report.views.factories.ReportViewFactory;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.TermService;
import org.javers.core.diff.Diff;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class ReportViewController {

	@Inject ReportViewFactory reportViewFactory;
	@Inject TermService termService;

	/**
	 * Delivers the available termStates for the initial report form.
	 *
	 * @param workgroupId
	 * @param httpResponse
     * @return
     */
	@RequestMapping(value = "/api/reportView/workgroups/{workgroupId}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Term> getTermsToCompare(@PathVariable long workgroupId, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return termService.findActiveTermCodesByWorkgroupId(workgroupId);
	}

	/**
	 * Delivers the JSON payload for the Diff View.
	 *
	 * @param workgroupId
	 * @param year
	 * @param termCode
	 * @param httpResponse
     * @return
     */
	@RequestMapping(value = "/api/diffView/workgroups/{workgroupId}/years/{year}/termCode/{termCode}", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	public List<Diff> showDiffView(@PathVariable long workgroupId, @PathVariable long year,
								   @PathVariable String termCode, HttpServletResponse httpResponse) {
		Authorizer.hasWorkgroupRoles(workgroupId, "academicPlanner", "reviewer");

		return reportViewFactory.createDiffView(workgroupId, year, termCode);
	}


}
