ALTER TABLE `BudgetScenarios`
    ADD COLUMN `isSnapshot` TINYINT(1) NOT NULL,
    ADD COLUMN `TaCost` FLOAT NULL,
    ADD COLUMN `ReaderCost` FLOAT NULL;

ALTER TABLE `InstructorCosts`
    ADD COLUMN `BudgetScenarioId` INT(11) NULL,
    MODIFY `BudgetId` INT(11) NULL;

ALTER TABLE `InstructorTypeCosts`
    ADD COLUMN `BudgetScenarioId` INT(11) NULL,
    MODIFY `BudgetId` INT(11) NULL;
