CREATE TABLE IF NOT EXISTS `CourseOverlaps` (
  `CourseOverlapsId` INT NOT NULL AUTO_INCREMENT,
  `CoursesA_CourseId` BIGINT UNSIGNED NOT NULL,
  `CoursesB_CourseId` BIGINT UNSIGNED NOT NULL,
   PRIMARY KEY (`CourseOverlapsId`),
  INDEX `CoursesA_CourseId_idx` (`CoursesA_CourseId` ASC),
  INDEX `CoursesB_CourseId_idx` (`CoursesB_CourseId` ASC),
  CONSTRAINT `CoursesA`
    FOREIGN KEY (`CoursesA_CourseId`)
    REFERENCES `Courses` (`CourseId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `CoursesB`
    FOREIGN KEY (`CoursesB_CourseId`)
    REFERENCES `Courses` (`CourseId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
    );
