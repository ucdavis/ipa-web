ALTER TABLE `SectionGroups` ADD COLUMN `TaAppointmentPercentage` INT(11) NULL;
ALTER TABLE `SectionGroups` ADD COLUMN `ReaderAppointmentPercentage` INT(11) NULL;
ALTER TABLE `SectionGroupCosts` ADD COLUMN `TaAppointmentPercentage` INT(11) NULL;
ALTER TABLE `SectionGroupCosts` ADD COLUMN `ReaderAppointmentPercentage` INT(11) NULL;
