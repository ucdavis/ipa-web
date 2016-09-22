ALTER TABLE `TeachingPreferences` DROP COLUMN `SectionGroups_SectionGroupId`;

-- Remove duplicate teachingPreferences (purposefully disabled - had issues with MySQL 5.7 and
-- no database exists anymore which needs this, especially not new ones)
--CREATE TABLE `TeachingPreferences_deduped` like `TeachingPreferences`;

--INSERT `TeachingPreferences_deduped` SELECT * FROM `TeachingPreferences` GROUP BY CourseOfferings_CourseOfferingId, Instructors_InstructorId;

--RENAME TABLE `TeachingPreferences` TO `TeachingPreferences_with_dupes`;
--RENAME TABLE `TeachingPreferences_deduped` TO `TeachingPreferences`;

ALTER TABLE `TeachingPreferences`
ADD UNIQUE INDEX `idx_unique_courseOffering_instructor` (`CourseOfferings_CourseOfferingId`, `Instructors_InstructorId`);

--DROP TABLE `TeachingPreferences_with_dupes`;