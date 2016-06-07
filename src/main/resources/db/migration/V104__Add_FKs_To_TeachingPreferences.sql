ALTER TABLE `TeachingPreferences`
CHANGE COLUMN `Courses_CourseId` `Courses_CourseId` BIGINT(20) NULL DEFAULT NULL ,
ADD INDEX `fk_teachingPreferences_instructorId_idx` (`Instructors_InstructorId` ASC),
ADD INDEX `fk_teachingPreferences_courseOfferingId_idx` (`CourseOfferings_CourseOfferingId` ASC);
ALTER TABLE `TeachingPreferences`
ADD CONSTRAINT `fk_teachingPreferences_courseOfferingId`
  FOREIGN KEY (`CourseOfferings_CourseOfferingId`)
  REFERENCES `CourseOfferings` (`CourseOfferingId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `fk_teachingPreferences_instructorId`
  FOREIGN KEY (`Instructors_InstructorId`)
  REFERENCES `Instructors` (`InstructorId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
