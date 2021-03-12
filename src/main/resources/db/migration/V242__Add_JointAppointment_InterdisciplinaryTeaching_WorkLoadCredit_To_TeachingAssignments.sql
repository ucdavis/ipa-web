ALTER TABLE `TeachingAssignments` ADD COLUMN `JointAppointment` TINYINT(1) NOT NULL DEFAULT '0';
ALTER TABLE `TeachingAssignments` ADD COLUMN `InterdisciplinaryTeaching` TINYINT(1) NOT NULL DEFAULT '0';
ALTER TABLE `TeachingAssignments` ADD COLUMN `WorkLoadCredit` TINYINT(1) NOT NULL DEFAULT '0';
