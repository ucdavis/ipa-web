ALTER TABLE `CourseOfferingGroups` 
ADD COLUMN `Title` VARCHAR(100) NULL,
ADD COLUMN `UnitsLow` FLOAT NULL DEFAULT NULL,
ADD COLUMN `UnitsHigh` FLOAT NULL DEFAULT NULL,
ADD COLUMN `CourseId` BIGINT(20) UNSIGNED NOT NULL,
ADD INDEX `Courses_CourseId_idx` (`CourseId` ASC);
