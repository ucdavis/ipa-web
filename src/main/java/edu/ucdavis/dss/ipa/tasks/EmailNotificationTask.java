package edu.ucdavis.dss.ipa.tasks;

import java.util.List;

import jakarta.inject.Inject;

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

	// Repeat every minute from 7:00:00am to 10:59:00pm
	@Scheduled( cron = "0 * 7-22 * * *", zone = "America/Los_Angeles")
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

		try {
			for (Long workgroupId : workgroupIds) {
				teachingCallReceiptService.sendNotificationsByWorkgroupId(workgroupId);
				studentSupportCallResponseService.sendNotificationsByWorkgroupId(workgroupId);
				instructorSupportCallResponseService.sendNotificationsByWorkgroupId(workgroupId);
			}
		} catch (Exception e) {
			// had SQL Out of Memory Exception that kept task from getting marked finished. ignore and try again.
			log.debug("Could not complete scanForEmailsToSend()");
			e.printStackTrace();
		} finally {
			runningTask = false;
			log.debug("scanForEmailsToSend() finished");
		}
	}
}
