ALTER TABLE `TeachingPreferences` 
ADD COLUMN `IsCourseRelease` TINYINT(1) NOT NULL DEFAULT '0' AFTER `IsBuyout`;
