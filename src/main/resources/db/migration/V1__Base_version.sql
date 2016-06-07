/*
Run the CREATE DATABASE command manually on your DB server before running the program

CREATE DATABASE IF NOT EXISTS IPA DEFAULT CHARACTER SET 'utf8'
  DEFAULT COLLATE 'utf8_unicode_ci';
  
*/

-- -----------------------------------------------------
-- Schema IPA
-- -----------------------------------------------------
-- DROP SCHEMA IF EXISTS `IPA` ;
-- CREATE SCHEMA IF NOT EXISTS `IPA` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci ;
-- USE `IPA` ;

-- -----------------------------------------------------
-- Table `IPA`.`Users`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Users` ;

CREATE TABLE `Users` (
  `UserId` INT NOT NULL AUTO_INCREMENT,
  `LoginId` VARCHAR(16) NOT NULL,
  `LastAccessed` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`UserId`));


-- -----------------------------------------------------
-- Table `IPA`.`Departments`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Departments` ;

CREATE TABLE `Departments` (
  `DepartmentId` INT NOT NULL AUTO_INCREMENT,
  `DepartmentName` VARCHAR(250) NOT NULL, /* (sometimes called Description) VERIFIED LENGTH, ICMS department names are capped at 250 */
  `DepartmentCode` VARCHAR(4) UNIQUE, /* VERIFIED LENGTH, needs to be nullable as ICMS won't have Department Codes' */
  `DepartmentNumber` VARCHAR(6) UNIQUE, /* (departmentNumber from LDAP) VERIFIED LENGTH */
  PRIMARY KEY (`DepartmentId`))
ENGINE = InnoDB;

CREATE UNIQUE INDEX `DepartmentId_UNIQUE` ON  `Departments` (`DepartmentId` ASC);


-- -----------------------------------------------------
-- Table `IPA`.`Users_has_Departments`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Users_has_Departments` ;

CREATE TABLE IF NOT EXISTS  `Users_has_Departments` (
  `Users_UserId` INT NOT NULL,
  `Departments_DepartmentId` INT NOT NULL,
  PRIMARY KEY (`Users_UserId`, `Departments_DepartmentId`),
  CONSTRAINT `fk_Users_has_Departments_Users`
    FOREIGN KEY (`Users_UserId`)
    REFERENCES  `Users` (`UserId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Users_has_Departments_Departments1`
    FOREIGN KEY (`Departments_DepartmentId`)
    REFERENCES  `Departments` (`DepartmentId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);

CREATE INDEX `fk_Users_has_Departments_Departments1_idx` ON  `Users_has_Departments` (`Departments_DepartmentId` ASC);


-- -----------------------------------------------------
-- Table `IPA`.`Schedules`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Schedules` ;

CREATE TABLE IF NOT EXISTS  `Schedules` (
  `ScheduleId` INT NOT NULL AUTO_INCREMENT,
  `Departments_DepartmentId` INT NOT NULL,
  `Year` INT NOT NULL,
  `State` INT NOT NULL,
  PRIMARY KEY (`ScheduleId`),
  CONSTRAINT `fk_Schedules_Departments1`
    FOREIGN KEY (`Departments_DepartmentId`)
    REFERENCES  `Departments` (`DepartmentId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  UNIQUE KEY `uk_Schedules_Departments_DepartmentId_Year` (`Departments_DepartmentId`,`Year`)
) ENGINE = InnoDB;

CREATE INDEX `fk_Schedules_Departments1_idx` ON  `Schedules` (`Departments_DepartmentId` ASC);


-- -----------------------------------------------------
-- Table `IPA`.`Programs`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Programs` ;

CREATE TABLE IF NOT EXISTS  `Programs` (
  `ProgramId` INT NOT NULL AUTO_INCREMENT,
  `Name` VARCHAR(45) NULL,
  `Departments_DepartmentId` INT NOT NULL,
  PRIMARY KEY (`ProgramId`),
  CONSTRAINT `fk_Programs_Departments1`
    FOREIGN KEY (`Departments_DepartmentId`)
    REFERENCES  `Departments` (`DepartmentId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_Programs_Departments1_idx` ON  `Programs` (`Departments_DepartmentId` ASC);


-- -----------------------------------------------------
-- Table `IPA`.`CourseOfferings`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`CourseOfferings` ;

CREATE TABLE IF NOT EXISTS  `CourseOfferings` (
  `CourseOfferingId` INT NOT NULL AUTO_INCREMENT,
  `Title` VARCHAR(30) NOT NULL,
  `SubjectCode` VARCHAR(4) NOT NULL,
  `CourseNumber` VARCHAR(7) NOT NULL,
  `UnitsLow` FLOAT NULL,
  `UnitsHigh` FLOAT NULL,
  `ScheduleId` INT NOT NULL,
  `TermCode` INT NOT NULL,
  PRIMARY KEY (`CourseOfferingId`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `IPA`.`Sections`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Sections` ;

CREATE TABLE IF NOT EXISTS  `Sections` (
  `SectionId` INT NOT NULL AUTO_INCREMENT,
  `Seats` INT NULL,
  `Crn` VARCHAR(5) NULL,
  `SequenceNumber` VARCHAR(3) NULL,
  `CourseOfferings_CourseOfferingId` INT NOT NULL,
  PRIMARY KEY (`SectionId`),
  CONSTRAINT `fk_Sections_CourseOfferings1`
    FOREIGN KEY (`CourseOfferings_CourseOfferingId`)
    REFERENCES  `CourseOfferings` (`CourseOfferingId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_Sections_CourseOfferings1_idx` ON  `Sections` (`CourseOfferings_CourseOfferingId` ASC);


-- -----------------------------------------------------
-- Table `IPA`.`Instructors`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Instructors` ;

CREATE TABLE IF NOT EXISTS  `Instructors` (
  `InstructorId` INT NOT NULL AUTO_INCREMENT,
  `FirstName` VARCHAR(45) NULL,
  `LastName` VARCHAR(45) NULL,
  `Email` VARCHAR(45) NULL,
  `LoginId` VARCHAR(45) NULL,
  `EmployeeId` VARCHAR(9) NULL,
  PRIMARY KEY (`InstructorId`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `IPA`.`Instructors_has_Sections`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`Instructors_has_Sections` ;

CREATE TABLE IF NOT EXISTS  `Instructors_has_Sections` (
  `Instructors_InstructorId` INT NOT NULL,
  `Sections_SectionId` INT NOT NULL,
  PRIMARY KEY (`Instructors_InstructorId`, `Sections_SectionId`),
  CONSTRAINT `fk_Instructors_has_Sections_Instructors1`
    FOREIGN KEY (`Instructors_InstructorId`)
    REFERENCES  `Instructors` (`InstructorId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Instructors_has_Sections_Sections1`
    FOREIGN KEY (`Sections_SectionId`)
    REFERENCES  `Sections` (`SectionId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_Instructors_has_Sections_Sections1_idx` ON  `Instructors_has_Sections` (`Sections_SectionId` ASC);

CREATE INDEX `fk_Instructors_has_Sections_Instructors1_idx` ON  `Instructors_has_Sections` (`Instructors_InstructorId` ASC);




-- -----------------------------------------------------
-- Table `IPA`.`CourseOfferings_has_Programs`
-- -----------------------------------------------------
-- DROP TABLE IF EXISTS `IPA`.`CourseOfferings_has_Programs`;

CREATE TABLE IF NOT EXISTS  `CourseOfferings_has_Programs` (
  `CourseOfferings_CourseOfferingId` INT NOT NULL,
  `Programs_ProgramId` INT NOT NULL,
  PRIMARY KEY (`CourseOfferings_CourseOfferingId`, `Programs_ProgramId`),
  CONSTRAINT `fk_CourseOfferings_has_Programs_CourseOfferings1`
    FOREIGN KEY (`CourseOfferings_CourseOfferingId`)
    REFERENCES  `CourseOfferings` (`CourseOfferingId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CourseOfferings_has_Programs_Programs1`
    FOREIGN KEY (`Programs_ProgramId`)
    REFERENCES  `Programs` (`ProgramId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

CREATE INDEX `fk_CourseOfferings_has_Programs_CourseOfferings1_idx` ON  `CourseOfferings_has_Programs` (`CourseOfferings_CourseOfferingId` ASC);

CREATE INDEX `fk_CourseOfferings_has_Programs_Programs1_idx` ON  `CourseOfferings_has_Programs` (`Programs_ProgramId` ASC);





-- SET SQL_MODE=@OLD_SQL_MODE;
-- SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
-- SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
