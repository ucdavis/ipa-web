ALTER TABLE `SyncActions`
DROP FOREIGN KEY `fk_SyncAction_Sections`;
ALTER TABLE `SyncActions`
CHANGE COLUMN `Id` `Id` INT(11) NOT NULL AUTO_INCREMENT ;
ALTER TABLE `SyncActions`
ADD CONSTRAINT `fk_SyncAction_Sections`
  FOREIGN KEY (`SectionId`)
  REFERENCES `Sections` (`Id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;