CREATE TABLE `ExpenseItems`(
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Amount` int(11) NOT NULL,
  `Description` varchar(50) NOT NULL,
  `Notes` varchar(50) NULL,
  `BudgetScenarioId` int(11) NOT NULL,
  `ExpenseItemTypeId` int(11) NULL,
  `TermCode` varchar(6) NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);

CREATE TABLE `ExpenseItemTypes` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Description` varchar(50) NOT NULL
);

INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('TA Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Reader Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Associate Instructor Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Continuing Lecturer Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Emeriti Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Instructor Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Ladder Faculty Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Lecturer SOE Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Unit 18 Pre-Six Lecturer Cost');
INSERT INTO `ExpenseItemTypes` (`Description`) VALUES ('Visiting Professor Cost');
