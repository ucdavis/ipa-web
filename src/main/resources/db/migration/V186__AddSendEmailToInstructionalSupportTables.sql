ALTER TABLE `InstructorSupportCallResponses` ADD COLUMN `SendEmail` boolean not null default false;
UPDATE `InstructorSupportCallResponses` SET SendEmail=1 WHERE Message IS NOT NULL;

ALTER TABLE `StudentSupportCallResponses` ADD COLUMN `SendEmail` boolean not null default false;
UPDATE `StudentSupportCallResponses` SET SendEmail=1 WHERE Message IS NOT NULL;
