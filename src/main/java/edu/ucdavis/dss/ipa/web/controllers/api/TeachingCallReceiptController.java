package edu.ucdavis.dss.ipa.web.controllers.api;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.entities.TeachingCallReceipt;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.TeachingCallReceiptService;
import edu.ucdavis.dss.ipa.utilities.UserLogger;
import edu.ucdavis.dss.ipa.web.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.web.views.TeachingCallReceiptViews;

@RestController
public class TeachingCallReceiptController {
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject AuthenticationService authenticationService;
	@Inject InstructorService instructorService;
	@Inject CurrentUser currentUser;

	@PreAuthorize("hasPermission(#teachingCallId, 'teachingCall', 'senateInstructor')"
			+ "or hasPermission(#teachingCallId, 'teachingCall', 'federationInstructor')")
	@RequestMapping(value = "/api/teachingCalls/{teachingCallId}/teachingCallReceipt", method = RequestMethod.GET, produces="application/json")
	@ResponseBody
	@JsonView(TeachingCallReceiptViews.Detailed.class)
	public TeachingCallReceipt getTeachingCallReceiptForLoggedInInstructor(
			@PathVariable long teachingCallId,
			HttpServletResponse httpResponse) {
		String loginId = authenticationService.getCurrentUser().getLoginid();
		return teachingCallReceiptService.findOrCreateByTeachingCallIdAndInstructorLoginId(teachingCallId, loginId);
	}

	@PreAuthorize("hasPermission(#teachingCallReceiptId, 'teachingCallReceipt', 'senateInstructor')"
			+ "or hasPermission(#teachingCallReceiptId, 'teachingCallReceipt', 'federationInstructor')")
	@RequestMapping(value = "/api/teachingCallReceipts/{teachingCallReceiptId}", method = RequestMethod.PUT)
	@ResponseBody
	@JsonView(TeachingCallReceiptViews.Detailed.class)
	public TeachingCallReceipt updateTeachingCallReceipt(
			@PathVariable Long teachingCallReceiptId,
			@RequestBody TeachingCallReceipt teachingCallReceipt,
			HttpServletResponse httpResponse) {
		TeachingCallReceipt toBeSaved = this.teachingCallReceiptService.findOneById(teachingCallReceiptId);

		if (toBeSaved == null) {
			httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
			return null;
		}

		toBeSaved.setIsDone(teachingCallReceipt.getIsDone());
		toBeSaved.setComment(teachingCallReceipt.getComment());

		UserLogger.log(currentUser, "Updated the teaching call receipt for the " + toBeSaved.getTeachingCall().getSchedule().getYear() + " schedule.");

		return this.teachingCallReceiptService.save(toBeSaved);
	}
}
