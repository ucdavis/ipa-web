CREATE TABLE GraduateStudents (
  `GraduateStudentId` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `FirstName` VARCHAR(45) NULL,
  `LastName` VARCHAR(45) NULL,
  `Departments_DepartmentId` INT NULL,
  `LoginId` VARCHAR(16) NOT NULL
);
ALTER TABLE `GraduateStudents` ADD INDEX `GraduateStudents_LoginId` (`LoginId`);
