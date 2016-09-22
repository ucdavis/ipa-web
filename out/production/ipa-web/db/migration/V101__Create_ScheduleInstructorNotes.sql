ALTER TABLE `TeachingCallReceipts` DROP COLUMN `assignmentsCompleted`;

CREATE TABLE `ScheduleInstructorNotes` (
  `ScheduleInstructorNoteId` INT(11) NOT NULL AUTO_INCREMENT,
  `Instructors_InstructorId` INT(11) NOT NULL,
  `Schedules_ScheduleId` INT(11) NOT NULL,
  `assignmentsCompleted` TINYINT(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`ScheduleInstructorNoteId`),
  INDEX `instructor_notes_fk_idx` (`Instructors_InstructorId` ASC),
  INDEX `schedule_notes_fk_idx` (`Schedules_ScheduleId` ASC),
  CONSTRAINT `instructor_notes_fk`
    FOREIGN KEY (`Instructors_InstructorId`)
    REFERENCES `Instructors` (`InstructorId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `schedule_notes_fk`
    FOREIGN KEY (`Schedules_ScheduleId`)
    REFERENCES `Schedules` (`ScheduleId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);