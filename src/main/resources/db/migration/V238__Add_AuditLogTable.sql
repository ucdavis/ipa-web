CREATE TABLE `AuditLog` (
  `Id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `LoginId` varchar(16) NOT NULL,
  `UserName` VARCHAR(100) NOT NULL,
  `Message` VARCHAR(2000) NOT NULL,
  `WorkgroupId` int(11) NOT NULL,
  `Year` int(11) NOT NULL,
  `Module` VARCHAR(100) NOT NULL,
  `TransactionId` BINARY(16) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL,
  INDEX `AuditLog_WorkgroupId_idx` (`WorkgroupId` ASC),
  INDEX `AuditLog_Module_idx` (`Module` ASC)
  );