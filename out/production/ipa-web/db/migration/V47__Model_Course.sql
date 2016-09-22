CREATE TABLE Courses (
  CourseId BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `SubjectCode` VARCHAR(4) NOT NULL,
  `CourseNumber` VARCHAR(7) NOT NULL,
  `EffectiveTermCode` VARCHAR(6) NOT NULL,
  `DepartmentId` INT NOT NULL,
  `Title` VARCHAR(30) NOT NULL,
  UNIQUE KEY uni_courses_row (SubjectCode, CourseNumber, EffectiveTermCode)
);
