CREATE TABLE `StudentSupportCallCrns` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `StudentSupportCallResponseId` int(11) NOT NULL,
  `Crn` VARCHAR(5) NOT NULL,
  `UpdatedAt` TIMESTAMP NULL,
  `CreatedAt` TIMESTAMP NULL,
  `ModifiedBy` VARCHAR(16) NULL
  );