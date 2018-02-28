ALTER TABLE `TeachingCallReceipts` ADD COLUMN `SendEmail` boolean not null default false;

UPDATE `TeachingCallReceipts` SET SendEmail=1 WHERE Message IS NOT NULL;
