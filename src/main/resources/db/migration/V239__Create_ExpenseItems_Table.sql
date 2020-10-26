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

INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Additional Funds From Deans Office');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Buyout Lecturer Replacement Funds');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Class Cancelled - Funds no longer needed');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('TA Range Adjustment Funds');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Work-Life Balance Funds');
INSERT INTO `ExpenseItemCategories` (`Description`) VALUES ('Other Funds');