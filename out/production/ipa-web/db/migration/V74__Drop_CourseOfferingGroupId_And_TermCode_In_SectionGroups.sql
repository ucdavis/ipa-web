ALTER TABLE `SectionGroups` 
DROP COLUMN `CourseOfferingGroupId`,
DROP COLUMN `TermCode`,
ADD INDEX `SectionGroups_CourseOfferings_CourseOfferingId_idx` (`CourseOfferings_CourseOfferingId` ASC);

ALTER TABLE `SectionGroups` 
ADD CONSTRAINT `CourseOfferings_CourseOfferingId`
  FOREIGN KEY (`CourseOfferings_CourseOfferingId`)
  REFERENCES `CourseOfferings` (`CourseOfferingId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
