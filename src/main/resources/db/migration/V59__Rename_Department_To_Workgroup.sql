-- Drop Foreign keys
ALTER TABLE `InstructorDepartmentRelationships` 
DROP FOREIGN KEY `DepartmentId`;

ALTER TABLE `Subjects_has_Departments` 
DROP FOREIGN KEY `fk_Subjects_has_Departments_Departments1`;

ALTER TABLE `Schedules` 
DROP FOREIGN KEY `fk_Schedules_Departments1`;

ALTER TABLE `Tracks` 
DROP FOREIGN KEY `fk_Tracks_Departments1`;



RENAME TABLE `Departments` TO  `Workgroups`;

ALTER TABLE `Workgroups` 
CHANGE COLUMN `DepartmentId` `WorkgroupId` INT(11) NOT NULL AUTO_INCREMENT ,
CHANGE COLUMN `DepartmentName` `WorkgroupName` VARCHAR(250) NOT NULL ,
CHANGE COLUMN `DepartmentCode` `WorkgroupCode` VARCHAR(4) NOT NULL ,
CHANGE COLUMN `DepartmentNumber` `WorkgroupNumber` VARCHAR(6) NULL DEFAULT NULL ;



RENAME TABLE `InstructorDepartmentRelationships` TO  `InstructorWorkgroupRelationships` ;

ALTER TABLE `InstructorWorkgroupRelationships` 
CHANGE COLUMN `InstructorDepartmentRelationshipId` `InstructorWorkgroupRelationshipId` INT(11) NOT NULL AUTO_INCREMENT ,
CHANGE COLUMN `Departments_DepartmentId` `Workgroups_WorkgroupId` INT(11) NOT NULL;

ALTER TABLE `InstructorWorkgroupRelationships` 
ADD CONSTRAINT `WorkgroupId`
  FOREIGN KEY (`Workgroups_WorkgroupId`)
  REFERENCES `Workgroups` (`WorkgroupId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



RENAME TABLE `Subjects_has_Departments` TO  `Subjects_has_Workgroups` ;

ALTER TABLE `Subjects_has_Workgroups` 
CHANGE COLUMN `Departments_DepartmentId` `Workgroups_WorkgroupId` INT(11) NOT NULL;

ALTER TABLE `Subjects_has_Workgroups` 
ADD CONSTRAINT `fk_Subjects_has_Workgroups`
  FOREIGN KEY (`Workgroups_WorkgroupId`)
  REFERENCES `Workgroups` (`WorkgroupId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



ALTER TABLE `Courses` 
CHANGE COLUMN `DepartmentId` `Workgroups_WorkgroupId` INT(11) NOT NULL ;



ALTER TABLE `GraduateStudents` 
CHANGE COLUMN `Departments_DepartmentId` `Workgroups_WorkgroupId` INT(11) NULL DEFAULT NULL ;



ALTER TABLE `Schedules` 
CHANGE COLUMN `Departments_DepartmentId` `Workgroups_WorkgroupId` INT(11) NOT NULL ;

ALTER TABLE `Schedules` 
ADD CONSTRAINT `fk_Schedules_Workgroups`
  FOREIGN KEY (`Workgroups_WorkgroupId`)
  REFERENCES `Workgroups` (`WorkgroupId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



ALTER TABLE `Tracks` 
CHANGE COLUMN `Departments_DepartmentId` `Workgroups_WorkgroupId` INT(11) NOT NULL ;

ALTER TABLE `Tracks` 
ADD CONSTRAINT `fk_Tracks_Workgroups`
  FOREIGN KEY (`Workgroups_WorkgroupId`)
  REFERENCES `Workgroups` (`WorkgroupId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;



ALTER TABLE `UserRoles` 
CHANGE COLUMN `Departments_DepartmentId` `Workgroups_WorkgroupId` INT(11) NULL DEFAULT NULL ;
