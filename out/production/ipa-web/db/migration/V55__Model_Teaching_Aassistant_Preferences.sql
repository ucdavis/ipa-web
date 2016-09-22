CREATE TABLE IF NOT EXISTS `TeachingAssistantPreferences` (
  `TeachingAssistantPreferenceId` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `GraduateStudents_GraduateStudentId` INT NULL,
  `CourseOfferings_CourseOfferingId` INT NULL,
  `Rank` INT NOT NULL,
  `Approved` BOOL NOT NULL DEFAULT 0,
  CONSTRAINT `fk_TeachingAssistantPreferences_GraduateStudents` 
    FOREIGN KEY (`GraduateStudents_GraduateStudentId`) 
    REFERENCES `GraduateStudents` (`GraduateStudentId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TeachingAssistantPreferences_CourseOfferings` 
    FOREIGN KEY (`CourseOfferings_CourseOfferingId`) 
    REFERENCES `CourseOfferings` (`CourseOfferingId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
ALTER TABLE `TeachingAssistantPreferences` ADD INDEX `TeachingAssistantPreferences_GraduateStudents_GraduateStudentId` (`GraduateStudents_GraduateStudentId`);