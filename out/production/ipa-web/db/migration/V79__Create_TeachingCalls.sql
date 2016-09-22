CREATE TABLE `TeachingCalls` (
  `TeachingCallId` INT(11) NOT NULL AUTO_INCREMENT,
  `Schedules_ScheduleId` INT(11) NOT NULL,
  `StartDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DueDate` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `Message` TEXT NULL,
  `SentToFederation` BOOL NOT NULL DEFAULT 0,
  `SentToSenate` BOOL NOT NULL DEFAULT 0,
  PRIMARY KEY (`TeachingCallId`),
  INDEX `ScheduleId_idx` (`Schedules_ScheduleId` ASC),
  CONSTRAINT `ScheduleId`
    FOREIGN KEY (`Schedules_ScheduleId`)
    REFERENCES `Schedules` (`ScheduleId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

ALTER TABLE `TeachingCallStatuses`
ADD COLUMN `TeachingCalls_TeachingCallId` INT(11) NOT NULL,
ADD UNIQUE INDEX `TeachingCall_TermCode_Schedule` (`Schedules_ScheduleId` ASC, `TermCode` ASC, `TeachingCalls_TeachingCallId` ASC),
ADD INDEX `TeachingCallStatuses _TeachingCallId_idx` (`TeachingCalls_TeachingCallId` ASC),
ADD INDEX `TeachingCallStatuses_InstructorId_idx` (`Instructors_InstructorId` ASC),
ADD INDEX `TeachingCallStatuses_Schedule_idx` (`Schedules_ScheduleId` ASC);



TRUNCATE `TeachingCallStatuses`;



ALTER TABLE `TeachingCallStatuses`
ADD CONSTRAINT `TeachingCallStatuses_TeachingCallId`
  FOREIGN KEY (`TeachingCalls_TeachingCallId`)
  REFERENCES `TeachingCalls` (`TeachingCallId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,

ADD CONSTRAINT `TeachingCallStatuses_Instructor`
  FOREIGN KEY (`Instructors_InstructorId`)
  REFERENCES `Instructors` (`InstructorId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,

ADD CONSTRAINT `TeachingCallStatuses_Schedule`
  FOREIGN KEY (`Schedules_ScheduleId`)
  REFERENCES `Schedules` (`ScheduleId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
