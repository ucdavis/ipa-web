CREATE TABLE `Notices` (
  `Id` INT(11) NOT NULL AUTO_INCREMENT,
  `WorkgroupId` INT(11) NOT NULL,
  `Description` TEXT NOT NULL,
  `DateTime` DATETIME NOT NULL DEFAULT Now(),
  `UserId` INT(11) NOT NULL,
  `Type` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`Id`),
  INDEX `fk_Notices_Users_idx` (`UserId` ASC),
  INDEX `fk_Notices_Workgroups_idx` (`WorkgroupId` ASC),
  CONSTRAINT `fk_Notices_Users`
  FOREIGN KEY (`UserId`)
  REFERENCES `Users` (`Id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Notices_Workgroups`
  FOREIGN KEY (`WorkgroupId`)
  REFERENCES `Workgroups` (`Id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);


ALTER TABLE `Instructors`
  CHANGE COLUMN `EmployeeId` `UcdStudentSID` VARCHAR(9) NULL DEFAULT NULL ;
