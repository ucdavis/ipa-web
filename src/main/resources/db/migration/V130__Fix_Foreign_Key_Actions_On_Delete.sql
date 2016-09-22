ALTER TABLE `Courses_has_Tags` 
DROP FOREIGN KEY `fk_Courses_has_Tracks`;
ALTER TABLE `Courses_has_Tags` 
ADD CONSTRAINT `fk_Courses_has_Tags`
  FOREIGN KEY (`TagId`)
  REFERENCES `Tags` (`Id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION,
ADD CONSTRAINT `fk_Tags_has_Courses`
  FOREIGN KEY (`CourseId`)
  REFERENCES `Courses` (`Id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;


ALTER TABLE `ScheduleInstructorNotes` 
DROP FOREIGN KEY `instructor_notes_fk`,
DROP FOREIGN KEY `schedule_notes_fk`;
ALTER TABLE `ScheduleInstructorNotes` 
ADD INDEX `schedule_notes_fk_idx` (`ScheduleId` ASC),
DROP INDEX `schedule_notes_fk_idx` ;
ALTER TABLE `ScheduleInstructorNotes` 
ADD CONSTRAINT `instructor_notes_fk`
  FOREIGN KEY (`InstructorId`)
  REFERENCES `Instructors` (`Id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION,
ADD CONSTRAINT `schedule_notes_fk`
  FOREIGN KEY (`ScheduleId`)
  REFERENCES `Schedules` (`Id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;
