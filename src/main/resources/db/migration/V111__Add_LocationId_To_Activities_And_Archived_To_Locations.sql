ALTER TABLE `Activities`
ADD COLUMN `Locations_LocationId` INT(11) NULL AFTER `Shared`,
ADD INDEX `fk_Activities_Locations_idx` (`Locations_LocationId` ASC);
ALTER TABLE `Activities`
ADD CONSTRAINT `fk_Activities_Locations`
  FOREIGN KEY (`Locations_LocationId`)
  REFERENCES `Locations` (`LocationId`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

ALTER TABLE `Locations`
ADD COLUMN `archived` TINYINT(1) NOT NULL DEFAULT '0' AFTER `Workgroups_WorkgroupId`;
