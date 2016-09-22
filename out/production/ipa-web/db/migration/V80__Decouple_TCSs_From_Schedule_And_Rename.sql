ALTER TABLE `TeachingCallStatuses` DROP FOREIGN KEY `TeachingCallStatuses_TeachingCallId`,
DROP FOREIGN KEY `TeachingCallStatuses_Instructor`;


ALTER TABLE `TeachingCallStatuses`
CHANGE COLUMN `TeachingCallStatusId` `TeachingCallResponseId` INT(11) NOT NULL AUTO_INCREMENT ,
DROP FOREIGN KEY `TeachingCallStatuses_Schedule`;

ALTER TABLE `TeachingCallStatuses`
DROP COLUMN `Schedules_ScheduleId`,
DROP INDEX `TeachingCallStatuses_Schedule_idx` ,
DROP INDEX `TeachingCall_TermCode_Schedule` ,
ADD UNIQUE INDEX `TeachingCall_Instructor_TermCode` (`TeachingCalls_TeachingCallId` ASC, `TermCode` ASC, `Instructors_InstructorId` ASC) ,
RENAME TO  `TeachingCallResponses`;


ALTER TABLE `TeachingCallResponses`
ADD CONSTRAINT `TeachingCallResponses_TeachingCallId`
  FOREIGN KEY (`TeachingCalls_TeachingCallId`)
  REFERENCES `TeachingCalls` (`TeachingCallId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,

ADD CONSTRAINT `TeachingCallResponses_Instructor`
  FOREIGN KEY (`Instructors_InstructorId`)
  REFERENCES `Instructors` (`InstructorId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
