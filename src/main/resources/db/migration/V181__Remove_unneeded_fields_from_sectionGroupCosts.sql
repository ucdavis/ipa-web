ALTER TABLE `SectionGroupCosts` DROP COLUMN `Title`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `SubjectCode`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `CourseNumber`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `SequencePattern`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `TermCode`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `UnitsLow`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `UnitsHigh`;
ALTER TABLE `SectionGroupCosts` DROP COLUMN `EffectiveTermCode`;

ALTER TABLE `SectionGroupCosts` CHANGE COLUMN `TaCount` `TaCount` float NULL;
ALTER TABLE `SectionGroupCosts` CHANGE COLUMN `ReaderCount` `ReaderCount` float NULL;