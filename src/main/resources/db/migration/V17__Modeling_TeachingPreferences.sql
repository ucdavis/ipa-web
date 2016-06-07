CREATE TABLE IF NOT EXISTS  `TeachingPreferences` (
	`TeachingPreferenceId` INT NOT NULL AUTO_INCREMENT,
	`InstructorId` INT NOT NULL,
	`TermCode` INT NOT NULL,
	`CourseOfferingId` INT NOT NULL,
	`Priority` INT NOT NULL,
	`IsBuyout` BOOL NOT NULL,
	`IsSabbatical` BOOL NOT NULL,
	`Notes` VARCHAR(200) NOT NULL,
  PRIMARY KEY (`TeachingPreferenceId`))
ENGINE = InnoDB;
