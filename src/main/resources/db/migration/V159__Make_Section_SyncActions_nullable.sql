ALTER TABLE `SyncActions` CHANGE COLUMN `SectionId` `SectionId` INT(11) NULL;
ALTER TABLE `SyncActions` ADD COLUMN `SectionGroupId` INT(11) NULL;
ALTER TABLE `SyncActions` CHANGE COLUMN `SectionProperty` `SectionProperty` varchar(50) NULL;