ALTER TABLE `CourseOfferingGroups` 
CHANGE COLUMN `Title` `Title` VARCHAR(100) NOT NULL ;

ALTER TABLE `CourseOfferingGroups` 
ADD CONSTRAINT `Courses_CourseId`
  FOREIGN KEY (`CourseId`)
  REFERENCES `Courses` (`CourseId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
