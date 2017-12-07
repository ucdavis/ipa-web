ALTER TABLE `StudentSupportCallResponses` ADD COLUMN `AvailabilityBlob` VARCHAR(150) NULL;
/* Assumes domain of interest is 7am-10pm = 15hours * 5days = 75, and *2 for commas */
ALTER TABLE `StudentSupportCallResponses` ADD COLUMN `CollectAvailabilityByGrid` TINYINT(1) NOT NULL DEFAULT '0';
ALTER TABLE `StudentSupportCallResponses` ADD COLUMN `CollectAvailabilityByCrn` TINYINT(1) NOT NULL DEFAULT '0';