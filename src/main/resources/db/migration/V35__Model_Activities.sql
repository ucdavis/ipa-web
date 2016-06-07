CREATE TABLE IF NOT EXISTS  `Activities` (
	`ActivityId` INT NOT NULL AUTO_INCREMENT,
	`Sections_SectionId` INT NOT NULL,
	`Buildings_BuildingId` INT NOT NULL,
 	`ActivityTypeCode` VARCHAR(3) NULL,
	`Confirmed` BOOL NOT NULL DEFAULT 0,
	`Room` VARCHAR(20) NOT NULL,
	`BeginDate` DATE NOT NULL,
	`EndDate` DATE NOT NULL,
	`StartTime` TIME NOT NULL,
	`EndTime` TIME NOT NULL,
	`DayIndicator` VARCHAR(7) NOT NULL,
	PRIMARY KEY (`ActivityId`),
	CONSTRAINT `fk_Activities_Sections` 
		FOREIGN KEY (`Sections_SectionId`) 
		REFERENCES `Sections` (`SectionId`)
		ON DELETE NO ACTION
		ON UPDATE NO ACTION
) ENGINE = InnoDB;