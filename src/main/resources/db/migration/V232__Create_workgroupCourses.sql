CREATE TABLE `WorkgroupCourses` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `WorkgroupId` int(11) NOT NULL,
  `Title` VARCHAR(100) NOT NULL,
  `SubjectCode` VARCHAR(4) NOT NULL,
  `CourseNumber` VARCHAR(7) NOT NULL,
  `EffectiveTermCode` VARCHAR(6) NOT NULL
);