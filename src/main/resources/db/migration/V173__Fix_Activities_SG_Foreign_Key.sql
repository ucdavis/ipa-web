-- fk_Activities_SectionGroups erroneously mentions the database name in addition to the table name, breaking
-- instances where the database name is different, e.g. in testing.

ALTER TABLE `Activities` DROP FOREIGN KEY `fk_Activities_SectionGroups`;

ALTER TABLE `Activities`
      ADD CONSTRAINT `fk_Activities_SectionGroups`
        FOREIGN KEY (`SectionGroupId`)
        REFERENCES `SectionGroups` (`Id`)
        ON DELETE NO ACTION
        ON UPDATE NO ACTION;
