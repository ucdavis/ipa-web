CREATE TABLE `Locations` (
  `LocationId` INT(11) NOT NULL AUTO_INCREMENT,
  `Description` VARCHAR(50) NOT NULL,
  `Workgroups_WorkgroupId` INT(11) NOT NULL,
  PRIMARY KEY (`LocationId`),
  INDEX `fk_Location_Workgroups_idx` (`Workgroups_WorkgroupId` ASC),
  CONSTRAINT `fk_Location_Workgroups`
    FOREIGN KEY (`Workgroups_WorkgroupId`)
    REFERENCES `Workgroups` (`WorkgroupId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


DROP TABLE `Buildings`;

ALTER TABLE `Activities` 
DROP COLUMN `Buildings_BuildingId`,
CHANGE COLUMN `Room` `BannerLocation` VARCHAR(20) NULL DEFAULT NULL ;

UPDATE `Activities`
SET `BannerLocation` = "";