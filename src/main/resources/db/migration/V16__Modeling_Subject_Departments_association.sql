CREATE TABLE IF NOT EXISTS  `Subjects_has_Departments` (
	`Subjects_SubjectId` INT NOT NULL,
	`Departments_DepartmentId` INT NOT NULL,
	PRIMARY KEY (`Subjects_SubjectId`, `Departments_DepartmentId`),
	CONSTRAINT `fk_Subjects_has_Departments_Subjects`
		FOREIGN KEY (`Subjects_SubjectId`)
		REFERENCES  `Subjects` (`SubjectId`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION,
	CONSTRAINT `fk_Subjects_has_Departments_Departments1`
		FOREIGN KEY (`Departments_DepartmentId`)
		REFERENCES  `Departments` (`DepartmentId`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION);
