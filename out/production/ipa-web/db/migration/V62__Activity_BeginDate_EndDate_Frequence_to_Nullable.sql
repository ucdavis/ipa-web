ALTER TABLE Activities
CHANGE COLUMN `BeginDate` `BeginDate` DATE NULL ,
CHANGE COLUMN `EndDate` `EndDate` DATE NULL ,
CHANGE COLUMN `Frequency` `Frequency` INT NULL DEFAULT 1;