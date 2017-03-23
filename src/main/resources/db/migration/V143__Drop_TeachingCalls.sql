
ALTER TABLE `TeachingCallReceipts` DROP FOREIGN KEY `TeachingCallReceipts_TeachingCallId`;
ALTER TABLE `TeachingCallReceipts` DROP COLUMN `TeachingCallId`;
ALTER TABLE `TeachingCallReceipts` DROP COLUMN `NotifiedAt`;
ALTER TABLE `TeachingCallReceipts` DROP COLUMN `WarnedAt`;

ALTER TABLE `TeachingCallResponses` DROP FOREIGN KEY `TeachingCallResponses_TeachingCallId`;
ALTER TABLE `TeachingCallResponses` DROP COLUMN `TeachingCallId`;

DROP TABLE `TeachingCalls`;