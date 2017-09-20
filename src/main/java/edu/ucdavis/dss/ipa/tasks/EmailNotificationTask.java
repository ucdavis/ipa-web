package edu.ucdavis.dss.ipa.tasks;

import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.services.InstructorSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.StudentSupportCallResponseService;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.services.TeachingCallReceiptService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

/**
 * Periodically (5 min) checks all teachingCallReceipts to determine if any notification or warning emails need to be sent.
 * @author Lloyd Wheeler
 *
 */
@Service
@Profile({"production", "staging"})
public class EmailNotificationTask {
	@Inject WorkgroupService workgroupService;
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject
	StudentSupportCallResponseService studentSupportCallResponseService;
	@Inject
	InstructorSupportCallResponseService instructorSupportCallResponseService;

	private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

	@Scheduled( fixedDelay = 300000 ) // Every 5 min
	@Async
	public void scanForEmailsToSend() {
		if(runningTask) return; // avoid multiple concurrent jobs
		runningTask = true;

		List<Long> workgroupIds = workgroupService.findAllIds();

		for (Long workgroupId : workgroupIds) {
			teachingCallReceiptService.sendNotificationsByWorkgroupId(workgroupId);
			studentSupportCallResponseService.sendNotificationsByWorkgroupId(workgroupId);
			instructorSupportCallResponseService.sendNotificationsByWorkgroupId(workgroupId);
		}

		runningTask = false;
	}

}
