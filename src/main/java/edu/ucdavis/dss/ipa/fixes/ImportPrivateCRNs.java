package edu.ucdavis.dss.ipa.fixes;

// Do not uncomment. This fix was applied December 2, 2015 and is kept for historical purposes.
//public class ImportPrivateCRNs {
//	private static final Logger log = LogManager.getLogger();
//	
//	/**
//	 * Designed for one-time use.
//	 * 
//	 * Scans all schedules and assumes they were created without the update which added
//	 * private CRN imports. Uses DwClient to then import only private CRNs and add them
//	 * to the schedule as appropriate.
//	 * 
//	 * @param rootContext
//	 */
//	public static void importPrivateCRNs(ApplicationContext rootContext) {
//		ScheduleService scheduleService = (ScheduleService)rootContext.getBean(ScheduleService.class);
//		DataWarehouseRepository dwRepository = (DataWarehouseRepository)rootContext.getBean(DataWarehouseRepository.class);
//		DwScheduleService dwScheduleService = (DwScheduleService)rootContext.getBean(DwScheduleService.class);
//		
//		if(scheduleService == null) {
//			log.error("Unable to inject ScheduleService!");
//			return;
//		}
//
//		// Get a list of all schedules
//		List<Schedule> schedules = scheduleService.findAll();
//		if(schedules == null) {
//			log.error("findAll() returned null!");
//			return;
//		}
//		
//		int currentYear = Calendar.getInstance().get(Calendar.YEAR);
//
//		// For each schedule ...
//		for(Schedule schedule : schedules) {
//			Set<DwSectionGroup> dwSectionGroups = null;
//			
//			try {
//				// Retrieve the missing private CRNs ...
//				dwSectionGroups = dwRepository.getPrivateSectionGroupsByDeptCodeAndYear(schedule.getWorkgroup().getCode(), schedule.getYear());
//
//				if(dwSectionGroups != null) {
//					log.info("Received " + dwSectionGroups.size() + " section groups from DW.");
//				} else {
//					log.error("dwClient returned NULL section groups!");
//					return; // cannot continue past this point
//				}
//			} catch (Exception e) {
//				log.error("Exception occurred:");
//				log.error(e);
//				continue;
//			}
//
//			// Add the private CRNs to the schedule
//			for(DwSectionGroup dwCo : dwSectionGroups) {
//				dwScheduleService.addOrUpdateDwSectionGroupToSchedule(dwCo, schedule, schedule.getYear() <= currentYear);
//			}
//
//			scheduleService.saveSchedule(schedule);
//		}
//	}
//}
