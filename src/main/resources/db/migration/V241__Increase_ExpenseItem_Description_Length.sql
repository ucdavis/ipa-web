ALTER TABLE `ExpenseItems` CHANGE COLUMN `Description` `Description` VARCHAR(300) NULL;
ALTER TABLE `ExpenseItems` DROP COLUMN `Notes`;