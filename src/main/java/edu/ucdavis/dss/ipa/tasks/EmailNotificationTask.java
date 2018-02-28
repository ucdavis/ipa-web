package edu.ucdavis.dss.ipa.tasks;

import java.util.List;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.services.InstructorSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.StudentSupportCallResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@Profile({"production", "staging", "development"})
public class EmailNotificationTask {
	private final Logger log = LoggerFactory.getLogger("EmailNotificationTask");

	@Inject WorkgroupService workgroupService;
	@Inject TeachingCallReceiptService teachingCallReceiptService;
	@Inject StudentSupportCallResponseService studentSupportCallResponseService;
	@Inject InstructorSupportCallResponseService instructorSupportCallResponseService;

	private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

	@Scheduled( fixedDelay = 300000 ) // Every 5 minutes
	@Async
	public void scanForEmailsToSend() {
		if(runningTask) {
			log.debug("scanForEmailsToSend() won't run: task already running");
			return; // avoid multiple concurrent jobs
		} else {
			log.debug("scanForEmailsToSend() will run: task not already running");
		}
		runningTask = true;

		List<Long> workgroupIds = workgroupService.findAllIds();

		for (Long workgroupId : workgroupIds) {
			teachingCallReceiptService.sendNotificationsByWorkgroupId(workgroupId);
			studentSupportCallResponseService.sendNotificationsByWorkgroupId(workgroupId);
			instructorSupportCallResponseService.sendNotificationsByWorkgroupId(workgroupId);
		}

		runningTask = false;

		log.debug("scanForEmailsToSend() finished");
	}

}
