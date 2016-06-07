CREATE TABLE `TeachingCallReceipts` (
  `TeachingCallReceiptId` INT(11) NOT NULL AUTO_INCREMENT,
  `NotifiedAt` TIMESTAMP NULL,
  `WarnedAt` TIMESTAMP NULL,
  `isDone` TINYINT(1) NOT NULL DEFAULT 0,
  `TeachingCalls_TeachingCallId` INT(11) NOT NULL,
  `Instructors_InstructorId` INT(11) NOT NULL,
  PRIMARY KEY (`TeachingCallReceiptId`),
  INDEX `TeachingCallReceipts_TeachingCallId_idx` (`TeachingCalls_TeachingCallId` ASC),
  INDEX `TeachingCallReceipts_InstructorId_idx` (`Instructors_InstructorId` ASC),
  CONSTRAINT `TeachingCallReceipts_TeachingCallId`
    FOREIGN KEY (`TeachingCalls_TeachingCallId`)
    REFERENCES `TeachingCalls` (`TeachingCallId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `TeachingCallReceipts_InstructorId`
    FOREIGN KEY (`Instructors_InstructorId`)
    REFERENCES `Instructors` (`InstructorId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);



ALTER TABLE `TeachingCallResponses` 
DROP COLUMN `IsComplete`;
