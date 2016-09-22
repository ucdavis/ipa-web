CREATE TABLE IF NOT EXISTS  `Tracks` (
  `TrackId` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(45) NULL,
  `Departments_DepartmentId` INT NOT NULL,
  PRIMARY KEY (`TrackId`),
  CONSTRAINT `fk_Tracks_Departments1`
    FOREIGN KEY (`Departments_DepartmentId`)
    REFERENCES  `Departments` (`DepartmentId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
