ALTER TABLE `BudgetScenarios`
    ADD COLUMN `isSnapshot` TINYINT(1) NOT NULL,
    ADD COLUMN `TaCost` FLOAT NULL,
    ADD COLUMN `ReaderCost` FLOAT NULL,
    MODIFY `Name` VARCHAR(100);

ALTER TABLE `InstructorCosts`
    ADD COLUMN `BudgetScenarioId` INT(11) NULL;

ALTER TABLE `InstructorTypeCosts`
    ADD COLUMN `BudgetScenarioId` INT(11) NULL;