ALTER TABLE `Activities` DROP FOREIGN KEY `fk_Activities_Locations`, DROP FOREIGN KEY `fk_Activities_Sections`;
ALTER TABLE `Activities` CHANGE COLUMN `Sections_SectionId` `SectionId` INT(11) NOT NULL ,
  CHANGE COLUMN `Locations_LocationId` `LocationId` INT(11) NULL DEFAULT NULL ;

ALTER TABLE `Activities`
  DROP INDEX `fk_Activities_Sections` ,
  ADD INDEX `fk_Activities_Sections_idx` (`SectionId` ASC);

ALTER TABLE `Activities`
  ADD CONSTRAINT `fk_Activities_Locations` FOREIGN KEY (`LocationId`)
  REFERENCES `Locations` (`Id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  ADD CONSTRAINT `fk_Activities_Sections` FOREIGN KEY (`SectionId`)
  REFERENCES `Sections` (`Id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;