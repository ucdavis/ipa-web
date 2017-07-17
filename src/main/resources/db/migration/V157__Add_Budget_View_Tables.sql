CREATE TABLE `Budgets` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `ScheduleId` int(11) NOT NULL,
  `TaCost` FLOAT NULL,
  `ReaderCost` FLOAT NULL,
  `LecturerCost` FLOAT NULL
);

CREATE TABLE `BudgetScenarios` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Name` varchar(50) NOT NULL,
  `BudgetId` int(11) NOT NULL
);

CREATE TABLE `SectionGroupCosts` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `BudgetScenarioId` int(11) NOT NULL,
  `SectionGroupId` int(11) NULL,
  `Title` VARCHAR(30) NOT NULL,
  `SubjectCode` varchar(4) NOT NULL,
  `CourseNumber` varchar(7) NOT NULL,
  `SequencePattern` varchar(7) NOT NULL,
  `TermCode` VARCHAR(7) NOT NULL,
  `UnitsLow` FLOAT DEFAULT NULL,
  `UnitsHigh` FLOAT DEFAULT NULL,
  `EffectiveTermCode` varchar(6) NOT NULL,
  `InstructorId` int(11) NULL,
  `OriginalInstructorId` int(11) NULL,
  `TaCount` int(11) NULL,
  `ReaderCount` int(11) NULL,
  `SectionCount` int(11) NULL,
  `Enrollment` int(11) NULL,
  `InstructorCost` FLOAT NULL,
  `ReplacementReason` varchar(50) NULL
);


CREATE TABLE `LineItems` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Amount` int(11) NOT NULL,
  `Description` varchar(50) NOT NULL,
  `Notes` varchar(50) NULL,
  `BudgetScenarioId` int(11) NOT NULL,
  `LineItemCategoryId` int(11) NULL
);

CREATE TABLE `LineItemCategories` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Description` varchar(50) NOT NULL
);

CREATE TABLE `LineItemComments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Comment` varchar(200) NOT NULL,
  `UserId` int(11) NOT NULL,
  `AuthorName` varchar(50) NOT NULL
);

CREATE TABLE `SectionGroupCostComments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `BudgetScenarioId` int(11) NOT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `Comment` varchar(200) NOT NULL,
  `UserId` int(11) NOT NULL,
  `AuthorName` varchar(50) NOT NULL
);

CREATE TABLE `InstructorCosts` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `BudgetId` int(11) NOT NULL,
  `InstructorId` int(11) NOT NULL,
  `Cost` FLOAT NULL,
  `Lecturer` int(11) DEFAULT 0 NOT NULL
);