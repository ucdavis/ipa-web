ALTER TABLE `Activities` 
CHANGE COLUMN `Confirmed` `ActivityState` INT(11) NOT NULL DEFAULT '0' ;
