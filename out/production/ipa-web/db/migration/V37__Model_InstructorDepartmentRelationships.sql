CREATE TABLE IF NOT EXISTS `InstructorDepartmentRelationships` (
  `InstructorDepartmentRelationshipId` INT NOT NULL AUTO_INCREMENT,
  `Instructors_InstructorId` INT(11) NOT NULL,
  `Departments_DepartmentId` INT(11) NOT NULL,
  `Hidden` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`InstructorDepartmentRelationshipId`),
  INDEX `Departments_DepartmentId_idx` (`Departments_DepartmentId` ASC),
  INDEX `InstructorId_idx` (`Instructors_InstructorId` ASC),
  CONSTRAINT `DepartmentId`
    FOREIGN KEY (`Departments_DepartmentId`)
    REFERENCES `Departments` (`DepartmentId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `InstructorId`
    FOREIGN KEY (`Instructors_InstructorId`)
    REFERENCES `Instructors` (`InstructorId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
