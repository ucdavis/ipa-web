package edu.ucdavis.dss.ipa.tasks;

import org.springframework.stereotype.Service;

@Service
public class EnrollmentUpdateTask {
//	private static final Logger log = LogManager.getLogger();
//
//	@Inject ScheduleRepository scheduleRepository;
//	@Inject DwScheduleService dwScheduleService;
//	@Inject DataWarehouseRepository dwRepository;
//
//	private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
//
//	@Scheduled(cron="0 3 * * * *") // every day at 3am
//	@Async
//	public void updateCurrentEnrollments() {
//		if(runningTask) return; // avoid multiple concurrent jobs
//
//		runningTask = true;
//
//		// For all current terms (very recent, current, and near future terms), update census snapshots from DW
//		Calendar now = Calendar.getInstance();
//		int currentYear = now.get(Calendar.YEAR);
//		long scheduleUpdateCount = 0;
//
//		for(int i = currentYear + 1; i >= currentYear - 1; i--) {
//			List<Schedule> schedules = scheduleRepository.findByYear((long)i);
//
//			for(Schedule schedule : schedules) {
//				long startTime = new Date().getTime();
//				log.info("Updating census snapshots for schedule ID " + schedule.getId() + " (schedule year: " + schedule.getYear() + ", schedule workgroup code: " + schedule.getWorkgroup().getCode() + ")");
//
//				scheduleUpdateCount++;
//
//				Set<DwSectionGroup> dwSectionGroups = null;
//
//				try {
//					dwSectionGroups = dwRepository.getSectionGroupsByDeptCodeAndYear(schedule.getWorkgroup().getCode(), schedule.getYear());
//				} catch (Exception e) {
//					ExceptionLogger.logAndMailException(this.getClass().getName(), e);
//					continue;
//				}
//
//				if(dwSectionGroups != null) {
//					for(DwSectionGroup dwSg : dwSectionGroups) {
//						this.dwScheduleService.updateCensusSnapshotsForSectionGroupAndSchedule(dwSg, schedule.getId());
//					}
//				}
//
//				long stopTime = new Date().getTime();
//				log.info("Finished updating census snapshots for schedule ID " + schedule.getId() + " (schedule year: " + schedule.getYear() + ", schedule workgroup code: " + schedule.getWorkgroup().getCode() + "). Took " + ((stopTime - startTime) / 1000) + "s");
//			}
//		}
//
//		log.info("Finished updating census snapshots for " + scheduleUpdateCount + " schedules.");
//
//		runningTask = false;
//	}
}
