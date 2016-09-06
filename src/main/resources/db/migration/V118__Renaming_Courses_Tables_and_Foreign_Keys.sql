ALTER TABLE `Tracks` RENAME TO  `Tags` ;
ALTER TABLE `CourseOfferingGroups_has_Tracks` RENAME TO  `Courses_has_Tags` ;

ALTER TABLE `Courses_has_Tags` CHANGE COLUMN `CourseOfferingGroups_id` `CourseId` INT(10) UNSIGNED NOT NULL ;

ALTER TABLE `Courses_has_Tags` DROP FOREIGN KEY `fk_CourseOfferingGroups_has_Tracks_Tracks1`;
ALTER TABLE `Courses_has_Tags` CHANGE COLUMN `Tracks_TrackId` `TrackId` INT(11) NOT NULL ,
  DROP INDEX `fk_CourseOfferingGroups_has_Tracks_Tracks1` ,
  ADD INDEX `fk_Courses_Tracks_idx` (`TrackId` ASC);
ALTER TABLE `Courses_has_Tags` ADD CONSTRAINT `fk_Courses_has_Tracks`
  FOREIGN KEY (`TrackId`) REFERENCES `Tags` (`Id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;
