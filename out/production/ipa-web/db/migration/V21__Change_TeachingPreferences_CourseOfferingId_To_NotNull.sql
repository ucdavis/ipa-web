ALTER TABLE `TeachingPreferences` 
CHANGE COLUMN `CourseOfferings_CourseOfferingId` `CourseOfferings_CourseOfferingId` INT(11) NULL ,
CHANGE COLUMN `IsBuyout` `IsBuyout` TINYINT(1) NOT NULL DEFAULT 0 ,
CHANGE COLUMN `IsSabbatical` `IsSabbatical` TINYINT(1) NOT NULL DEFAULT 0 ,
CHANGE COLUMN `Notes` `Notes` VARCHAR(200) NULL DEFAULT '' ;
