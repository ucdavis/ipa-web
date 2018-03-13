ALTER TABLE `TeachingAssignments` ADD COLUMN `InstructorTypeId` int(11) NULL;

ALTER TABLE `TeachingAssignments` CHANGE COLUMN `InstructorId` `InstructorId` int(11) NULL;
