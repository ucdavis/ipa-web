CREATE TABLE IF NOT EXISTS InstructorTeachingAssistantPreferences (
	`InstructorTeachingAssistantPreferenceId` INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
	`GraduateStudents_GraduateStudentId` INT NULL,
	`CourseOfferings_CourseOfferingId` INT NULL,
	`Instructors_InstructorId` INT NULL,
	`Rank` INT NOT NULL,
		CONSTRAINT `fk_InstructorTeachingAssistantPreferences_GraduateStudents` 
		FOREIGN KEY (`GraduateStudents_GraduateStudentId`) 
		REFERENCES `GraduateStudents` (`GraduateStudentId`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION,
		CONSTRAINT `fk_InstructorTeachingAssistantPreferences_CourseOfferings` 
		FOREIGN KEY (`CourseOfferings_CourseOfferingId`) 
		REFERENCES `CourseOfferings` (`CourseOfferingId`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION,
		CONSTRAINT `fk_InstructorTeachingAssistantPreferences_Instructors` 
		FOREIGN KEY (`Instructors_InstructorId`) 
		REFERENCES `Instructors` (`InstructorId`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
);