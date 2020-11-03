CREATE TABLE `ExpenseItems`(
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Amount` int(11) NOT NULL,
  `Description` varchar(50) NOT NULL,
  `Notes` varchar(50) NULL,
  `BudgetScenarioId` int(11) NOT NULL,
  `ExpenseItemCategoryId` int(11) NULL,
  `TermCode` varchar(6) NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);

CREATE TABLE `ExpenseItemCategories` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Description` varchar(50) NOT NULL
);

INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('TA Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Reader Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Associate Instructor Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Continuing Lecturer Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Emeriti Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Instructor Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Ladder Faculty Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Lecturer SOE Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Unit 18 Pre-Six Lecturer Cost');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Visiting Professor Cost');
