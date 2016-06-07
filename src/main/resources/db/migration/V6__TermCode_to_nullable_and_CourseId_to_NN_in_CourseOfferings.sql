UPDATE CourseOfferings
SET `CourseId` = 0
WHERE `CourseId` = NULL;

ALTER TABLE CourseOfferings
CHANGE COLUMN `TermCode` `TermCode` VARCHAR(6) NULL ,
CHANGE COLUMN `CourseId` `CourseId` BIGINT(20) UNSIGNED NOT NULL ;
