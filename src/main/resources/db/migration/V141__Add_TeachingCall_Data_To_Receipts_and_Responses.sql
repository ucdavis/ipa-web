
-- Add columns to receipts
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `Message` TEXT NULL;
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `Termsblob` VARCHAR(10) DEFAULT '1111111111';
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `ShowUnavailabilities` TINYINT(1) NOT NULL DEFAULT '1';

-- Rename columns from receipts
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `Schedules_ScheduleId` INT(11) NOT NULL;
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `LastContactedAt` TIMESTAMP NULL;
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `NextContactAt` TIMESTAMP NULL;

-- Rename columns from responses
ALTER TABLE `TeachingCallResponses` ADD COLUMN `Schedules_ScheduleId` INT(11) NOT NULL;