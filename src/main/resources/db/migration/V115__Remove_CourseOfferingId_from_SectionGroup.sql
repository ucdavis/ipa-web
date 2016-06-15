SET foreign_key_checks = 0;
ALTER TABLE `CourseOfferingGroups` DROP FOREIGN KEY `Courses_CourseId`;
ALTER TABLE `CourseOfferingGroups` DROP FOREIGN KEY `fk_courseOfferingGroup_scheduleId`;
ALTER TABLE `CourseOverlaps` DROP FOREIGN KEY `CoursesA`;
ALTER TABLE `CourseOverlaps` DROP FOREIGN KEY `CoursesB`;
ALTER TABLE `SectionGroups` DROP FOREIGN KEY `CourseOfferings_CourseOfferingId`;
ALTER TABLE `CourseOfferingGroups_has_Tracks` DROP FOREIGN KEY `fk_CourseOfferingGroups_has_Tracks_CourseOfferingGroups`;

ALTER TABLE `SectionGroups` DROP COLUMN `CourseOfferings_CourseOfferingId`;
DROP TABLE `CourseOfferings`;
DROP TABLE `TeachingPreferences`;
DROP TABLE `Courses`;

ALTER TABLE `CourseOfferingGroups` DROP COLUMN `CourseId`;

ALTER TABLE `CourseOfferingGroups` RENAME TO `Courses` ;

SET foreign_key_checks = 1;