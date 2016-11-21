CREATE TABLE `SyncActions` (
  `Id` INT(11) NOT NULL,
  `SectionId` INT(11) NOT NULL,
  `SectionProperty` VARCHAR(25) NULL,
  `ChildProperty` VARCHAR(25) NULL,
  `ChildUniqueKey` VARCHAR(100) NULL,
  PRIMARY KEY (`Id`),
  CONSTRAINT `fk_SyncAction_Sections`
    FOREIGN KEY (`Id`)
    REFERENCES `Sections` (`Id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION);
