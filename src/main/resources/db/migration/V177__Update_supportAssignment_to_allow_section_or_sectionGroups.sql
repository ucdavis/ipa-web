ALTER TABLE `SupportAssignments` ADD COLUMN `SectionId` int(11) NULL;
ALTER TABLE `SupportAssignments` CHANGE COLUMN `SectionGroupId` `SectionGroupId` int(11) NULL;