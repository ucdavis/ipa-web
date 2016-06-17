SET foreign_key_checks = 0;

DROP TABLE `CensusSnapshots`;
DROP TABLE `CourseOverlaps`;
DROP TABLE `GraduateStudents`;
DROP TABLE `InstructorTeachingAssistantPreferences`;
DROP TABLE `InstructorWorkgroupRelationships`;
DROP TABLE `Subjects`;
DROP TABLE `Subjects_has_Workgroups`;
DROP TABLE `TeachingAssistantPreferences`;

-- Rename FK Workgroups_WorkgroupId to WorkgroupId in Locations
ALTER TABLE `Locations` 
DROP FOREIGN KEY `fk_Location_Workgroups`;
ALTER TABLE `Locations` 
CHANGE COLUMN `Workgroups_WorkgroupId` `WorkgroupId` INT(11) NOT NULL ;
ALTER TABLE `Locations` 
ADD CONSTRAINT `fk_Location_Workgroups`
  FOREIGN KEY (`WorkgroupId`)
  REFERENCES `Workgroups` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK Instructors_InstructorId and Schedules_ScheduleId in ScheduleInstructorNotes
ALTER TABLE `ScheduleInstructorNotes` 
DROP FOREIGN KEY `instructor_notes_fk`,
DROP FOREIGN KEY `schedule_notes_fk`;
ALTER TABLE `ScheduleInstructorNotes` 
CHANGE COLUMN `Instructors_InstructorId` `InstructorId` INT(11) NOT NULL ,
CHANGE COLUMN `Schedules_ScheduleId` `ScheduleId` INT(11) NOT NULL ;
ALTER TABLE `ScheduleInstructorNotes` 
ADD CONSTRAINT `instructor_notes_fk`
  FOREIGN KEY (`InstructorId`)
  REFERENCES `Instructors` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `schedule_notes_fk`
  FOREIGN KEY (`ScheduleId`)
  REFERENCES `Schedules` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK Workgroups_WorkgroupId to WorkgroupId in Schedules
ALTER TABLE `Schedules` 
DROP FOREIGN KEY `fk_Schedules_Workgroups`;
ALTER TABLE `Schedules` 
CHANGE COLUMN `Workgroups_WorkgroupId` `WorkgroupId` INT(11) NOT NULL ;
ALTER TABLE `Schedules` 
ADD CONSTRAINT `fk_Schedules_Workgroups`
  FOREIGN KEY (`WorkgroupId`)
  REFERENCES `Workgroups` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK SectionGroups_SectionGroupId to SectionGroupId in Sections
ALTER TABLE `Sections` 
DROP FOREIGN KEY `fk_Sections_SectionGroups`;
ALTER TABLE `Sections` 
CHANGE COLUMN `SectionGroups_SectionGroupId` `SectionGroupId` INT(11) NOT NULL ;
ALTER TABLE `Sections` 
ADD CONSTRAINT `fk_Sections_SectionGroups`
  FOREIGN KEY (`SectionGroupId`)
  REFERENCES `SectionGroups` (`Id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;

-- Rename FK TeachingCallAssignments_InstructorId and TeachingCallAssignments_SectionGroupId in TeachingAssignments
ALTER TABLE `TeachingAssignments`
DROP FOREIGN KEY `TeachingCallAssignments_InstructorId`,
DROP FOREIGN KEY `TeachingCallAssignments_SectionGroupId`;
ALTER TABLE `TeachingAssignments` 
CHANGE COLUMN `SectionGroups_SectionGroupId` `SectionGroupId` INT(11) NOT NULL ,
CHANGE COLUMN `Instructors_InstructorId` `InstructorId` INT(11) NOT NULL ;
ALTER TABLE `TeachingAssignments` 
ADD CONSTRAINT `TeachingCallAssignments_InstructorId`
  FOREIGN KEY (`InstructorId`)
  REFERENCES `Instructors` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `TeachingCallAssignments_SectionGroupId`
  FOREIGN KEY (`SectionGroupId`)
  REFERENCES `SectionGroups` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK TeachingCallReceipts_InstructorId and TeachingCallReceipts_TeachingCallId in TeachingCallReceipts
ALTER TABLE `TeachingCallReceipts`
DROP FOREIGN KEY `TeachingCallReceipts_InstructorId`,
DROP FOREIGN KEY `TeachingCallReceipts_TeachingCallId`;
ALTER TABLE `TeachingCallReceipts` 
CHANGE COLUMN `TeachingCalls_TeachingCallId` `TeachingCallId` INT(11) NOT NULL ,
CHANGE COLUMN `Instructors_InstructorId` `InstructorId` INT(11) NOT NULL ;
ALTER TABLE `TeachingCallReceipts` 
ADD CONSTRAINT `TeachingCallReceipts_InstructorId`
  FOREIGN KEY (`InstructorId`)
  REFERENCES `Instructors` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `TeachingCallReceipts_TeachingCallId`
  FOREIGN KEY (`TeachingCallId`)
  REFERENCES `TeachingCalls` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK TeachingCallResponses_Instructor and TeachingCallResponses_TeachingCallId in TeachingCallResponses
ALTER TABLE `TeachingCallResponses`
DROP FOREIGN KEY `TeachingCallResponses_Instructor`,
DROP FOREIGN KEY `TeachingCallResponses_TeachingCallId`;
ALTER TABLE `TeachingCallResponses` 
CHANGE COLUMN `Instructors_InstructorId` `InstructorId` INT(11) NOT NULL ,
CHANGE COLUMN `TeachingCalls_TeachingCallId` `TeachingCallId` INT(11) NOT NULL ;
ALTER TABLE `TeachingCallResponses` 
ADD CONSTRAINT `TeachingCallResponses_Instructor`
  FOREIGN KEY (`InstructorId`)
  REFERENCES `Instructors` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION,
ADD CONSTRAINT `TeachingCallResponses_TeachingCallId`
  FOREIGN KEY (`TeachingCallId`)
  REFERENCES `TeachingCalls` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK Schedules_ScheduleId to ScheduleId in TeachingCalls
ALTER TABLE `TeachingCalls`
DROP FOREIGN KEY `ScheduleId`;
ALTER TABLE `TeachingCalls` 
CHANGE COLUMN `Schedules_ScheduleId` `ScheduleId` INT(11) NOT NULL ;
ALTER TABLE `TeachingCalls` 
ADD CONSTRAINT `ScheduleId`
  FOREIGN KEY (`ScheduleId`)
  REFERENCES `Schedules` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK Workgroups_WorkgroupId to WorkgroupId in Tags
ALTER TABLE `Tags`
  DROP FOREIGN KEY `fk_Tracks_Workgroups`;
ALTER TABLE `Tags`
  CHANGE COLUMN `Workgroups_WorkgroupId` `WorkgroupId` INT(11) NOT NULL ;
ALTER TABLE `Tags`
  ADD CONSTRAINT `fk_Tracks_Workgroups`
FOREIGN KEY (`WorkgroupId`)
REFERENCES `Workgroups` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename FK Users_UserId, Workgroups_WorkgroupId and Roles_RoleId in UserRoles
ALTER TABLE `UserRoles` 
CHANGE COLUMN `Users_UserId` `UserId` INT(11) NOT NULL ,
CHANGE COLUMN `Workgroups_WorkgroupId` `WorkgroupId` INT(11) NULL DEFAULT NULL ,
CHANGE COLUMN `Roles_RoleId` `RoleId` INT(11) NOT NULL ;

ALTER TABLE `SectionGroups`
  DROP COLUMN `ReaderCount`,
  DROP COLUMN `TeachingAssistantCount`;

ALTER TABLE `Courses_has_Tags`
  DROP FOREIGN KEY `fk_Courses_has_Tracks`;
ALTER TABLE `Courses_has_Tags`
  CHANGE COLUMN `TrackId` `TagId` INT(11) NOT NULL ;
ALTER TABLE `Courses_has_Tags`
  ADD CONSTRAINT `fk_Courses_has_Tracks`
FOREIGN KEY (`TagId`)
REFERENCES `tags` (`Id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


SET foreign_key_checks = 1;
