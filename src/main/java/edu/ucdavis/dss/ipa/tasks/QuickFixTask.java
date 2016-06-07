package edu.ucdavis.dss.ipa.tasks;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.services.SectionGroupService;

@Service
public class QuickFixTask {
	private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
	private static final Logger log = LogManager.getLogger();
	@Inject SectionGroupService sectionGroupService;

	@Autowired private ApplicationContext applicationContext;

	@Scheduled( fixedDelay = 86400000 ) // Assume the task will finish within 1 day
	@Async
	public void runQuickFix() {
		if(runningTask) return; // avoid multiple concurrent jobs
		runningTask = true;
		
//		log.info("Running QuickFixTask: FixSharedActivities ...");
//		List<SectionGroup> sectionGroups = sectionGroupService.findAllEager();

//		FixSharedActivities.fixSharedActivities(sectionGroups, applicationContext);

//		log.info("Finished running QuickFixTask: FixSharedActivities.");

		runningTask = false;
	}
}