-- tracking previous manual updates in migration
UPDATE `LineItemCategories` SET Description = "Funds From Dean's Office" WHERE Id = 1;
UPDATE `LineItemCategories` SET Description = "Internal Buyout" WHERE Id = 2;
UPDATE `LineItemCategories` SET Description = "Range Adjustment Funds" WHERE Id = 4;

DELETE FROM `LineItemCategories` WHERE id >= 7;
INSERT INTO `LineItemCategories` (`Id`, `Description`) VALUES (7, "External Buyout");
INSERT INTO `LineItemCategories` (`Id`, `Description`) VALUES (8, "Additional Funds From Dean's Office");
INSERT INTO `LineItemCategories` (`Id`, `Description`) VALUES (9, "Funds not in GENT Account");

-- remove unused entries
DELETE FROM `ReasonCategories` WHERE id < 10;
UPDATE `SectionGroupCosts` SET `ReasonCategoryId` = 25 WHERE ReasonCategoryId = 4;
UPDATE `SectionGroupCosts` SET `ReasonCategoryId` = NULL WHERE ReasonCategoryId < 10;
