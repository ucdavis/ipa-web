CREATE TABLE `InstructorTypes` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `BudgetId` int(11) NOT NULL,
  `Cost` float DEFAULT NULL,
  `Description` varchar(50) NOT NULL,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `CreatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);