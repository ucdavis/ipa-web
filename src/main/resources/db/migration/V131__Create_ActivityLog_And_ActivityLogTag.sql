-- -----------------------------------------------------
-- Table `IPA`.`ActivityLog`
-- -----------------------------------------------------
CREATE TABLE `ActivityLog` (
  `Id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `UserId` INT(11) NOT NULL,
  `Message` VARCHAR(255) NOT NULL,
  `Timestamp` TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  CONSTRAINT `fk_ActivityLog_Users1`
  FOREIGN KEY (`UserId`)
  REFERENCES  `Users` (`Id`)
  );

CREATE INDEX `fk_ActivityLog_Users1_idx` ON  `ActivityLog` (`UserId` ASC);
CREATE INDEX `fk_ActivityLog_Timestamp_idx` ON  `ActivityLog` (`Timestamp` ASC);


-- -----------------------------------------------------
-- Table `IPA`.`ActivityLogTag`
-- -----------------------------------------------------
CREATE TABLE `ActivityLogTag` (
  `Id` INT(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `ActivityLogId` INT(11) NOT NULL,
  `Tag` VARCHAR(50) NOT NULL,
  CONSTRAINT `fk_ActivityLogTag_ActivityLog1`
  FOREIGN KEY (`ActivityLogId`)
  REFERENCES  `ActivityLog` (`Id`)
  );

CREATE INDEX `fk_ActivityLogTag_ActivityLog1_idx` ON  `ActivityLogTag` (`ActivityLogId` ASC);