ALTER TABLE `InstructorSupportCallResponses` ADD COLUMN `SendEmail` TINYINT(1) NOT NULL;
ALTER TABLE `StudentSupportCallResponses` ADD COLUMN `SendEmail` TINYINT(1) NOT NULL;
ALTER TABLE `TeachingCallReceipts` ADD COLUMN `SendEmail` TINYINT(1) NOT NULL;