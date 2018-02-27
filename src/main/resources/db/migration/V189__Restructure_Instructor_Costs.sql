ALTER TABLE `InstructorTypes` RENAME TO `InstructorTypeCosts`;
ALTER TABLE `InstructorTypeCosts` ADD COLUMN `InstructorTypeId` int(11) NULL;

ALTER TABLE `InstructorCosts` CHANGE COLUMN `InstructorTypeId` `InstructorTypeCostId` int(11) NULL;

CREATE TABLE `InstructorTypes` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Description` varchar(50) NOT NULL
);

INSERT INTO `InstructorTypes` (`Description`) VALUES ('Emeriti - Recalled');
INSERT INTO `InstructorTypes` (`Description`) VALUES ('Visiting Professor');
INSERT INTO `InstructorTypes` (`Description`) VALUES ('Associate Instructor');
INSERT INTO `InstructorTypes` (`Description`) VALUES ('Unit 18 Pre-Six Lecturer');
INSERT INTO `InstructorTypes` (`Description`) VALUES ('Continuing Lecturer');
INSERT INTO `InstructorTypes` (`Description`) VALUES ('Ladder Faculty');