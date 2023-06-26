ALTER TABLE `LineItems` ADD COLUMN `LineItemTypeId` INT(11) DEFAULT NULL;
ALTER TABLE `LineItems` ADD COLUMN `TermCode` VARCHAR(6) DEFAULT NULL;

CREATE TABLE `LineItemTypes` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Description` varchar(50) NOT NULL
);

INSERT INTO `LineItemTypes` (`Description`) VALUES ('TA');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Reader');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Associate Instructor');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Continuing Lecturer');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Emeriti');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Instructor');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Ladder Faculty');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Lecturer SOE');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Unit 18 Pre-Six Lecturer');
INSERT INTO `LineItemTypes` (`Description`) VALUES ('Visiting Professor');
