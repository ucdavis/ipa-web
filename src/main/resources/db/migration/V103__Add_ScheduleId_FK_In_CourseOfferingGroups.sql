ALTER TABLE `CourseOfferingGroups`
ADD INDEX `ScheduleId_idx` (`ScheduleId` ASC);
ALTER TABLE `CourseOfferingGroups`
ADD CONSTRAINT `fk_courseOfferingGroup_scheduleId`
  FOREIGN KEY (`ScheduleId`)
  REFERENCES `Schedules` (`ScheduleId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
