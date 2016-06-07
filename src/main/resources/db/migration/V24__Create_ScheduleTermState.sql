ALTER TABLE `Schedules` DROP COLUMN `State`;

CREATE TABLE `ScheduleTermStates` (
  `ScheduleTermStateId` INT NOT NULL AUTO_INCREMENT,
  `TermCode` VARCHAR(6) NOT NULL,
  `State` INT(11) NOT NULL,
  `Schedules_ScheduleId` INT(11) NOT NULL,
  PRIMARY KEY (`ScheduleTermStateId`),
  INDEX `fk_ScheduleTermStates_Schedules_idx` (`Schedules_ScheduleId` ASC),
  CONSTRAINT `fk_ScheduleTermStates_Schedules`
    FOREIGN KEY (`Schedules_ScheduleId`)
    REFERENCES `Schedules` (`ScheduleId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
