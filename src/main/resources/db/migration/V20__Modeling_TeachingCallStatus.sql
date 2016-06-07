CREATE TABLE IF NOT EXISTS  `TeachingCallStatuses` (
	`TeachingCallStatusId` INT NOT NULL AUTO_INCREMENT,
	`Instructors_InstructorId` INT NOT NULL,
	`Schedules_ScheduleId` INT NOT NULL,
	`TermCode` VARCHAR(6) NOT NULL,
	`AvailabilityBlob` VARCHAR(150) NOT NULL, /* Assumes domain of interest is 7am-10pm = 15hours * 5days = 75, and *2 for commas */
  PRIMARY KEY (`TeachingCallStatusId`))
ENGINE = InnoDB;
