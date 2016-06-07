ALTER TABLE `Sections` 
DROP FOREIGN KEY `fk_Sections_CourseOfferings1`;

ALTER TABLE `TeachingAssistantPreferences` 
DROP FOREIGN KEY `fk_TeachingAssistantPreferences_CourseOfferings`;

ALTER TABLE `InstructorTeachingAssistantPreferences` 
DROP FOREIGN KEY `fk_InstructorTeachingAssistantPreferences_CourseOfferings`;



RENAME TABLE `CourseOfferings` TO  `SectionGroups`;

ALTER TABLE `SectionGroups` 
CHANGE COLUMN `CourseOfferingId` `SectionGroupId` INT(11) NOT NULL AUTO_INCREMENT ;



ALTER TABLE `Sections` 
CHANGE COLUMN `CourseOfferings_CourseOfferingId` `SectionGroups_SectionGroupId` INT(11) NOT NULL ;

ALTER TABLE `Sections` 
ADD CONSTRAINT `fk_Sections_SectionGroups`
  FOREIGN KEY (`SectionGroups_SectionGroupId`)
  REFERENCES `SectionGroups` (`SectionGroupId`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;



ALTER TABLE `TeachingPreferences` 
CHANGE COLUMN `CourseOfferings_CourseOfferingId` `SectionGroups_SectionGroupId` INT(11) NULL DEFAULT NULL ;



ALTER TABLE `TeachingAssistantPreferences` 
CHANGE COLUMN `CourseOfferings_CourseOfferingId` `SectionGroups_SectionGroupId` INT(11) NULL DEFAULT NULL ;

ALTER TABLE `TeachingAssistantPreferences` 
ADD CONSTRAINT `fk_TeachingAssistantPreferences_SectionGroups`
  FOREIGN KEY (`SectionGroups_SectionGroupId`)
  REFERENCES `SectionGroups` (`SectionGroupId`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;



ALTER TABLE `InstructorTeachingAssistantPreferences` 
CHANGE COLUMN `CourseOfferings_CourseOfferingId` `SectionGroups_SectionGroupId` INT(11) NULL DEFAULT NULL ;

ALTER TABLE `InstructorTeachingAssistantPreferences` 
ADD CONSTRAINT `fk_InstructorTeachingAssistantPreferences_SectionGroups`
  FOREIGN KEY (`SectionGroups_SectionGroupId`)
  REFERENCES `SectionGroups` (`SectionGroupId`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;
