CREATE TABLE `CourseOfferings` (
  `CourseOfferingId` INT(11) NOT NULL AUTO_INCREMENT,
  `TermCode` VARCHAR(6) NOT NULL,
  `CourseOfferingGroupId` INT(11) UNSIGNED NOT NULL,
  PRIMARY KEY (`CourseOfferingId`),
  INDEX `CourseOfferingGroups_id_idx` (`CourseOfferingGroupId` ASC),
  CONSTRAINT `CourseOfferingGroups_id`
    FOREIGN KEY (`CourseOfferingGroupId`)
    REFERENCES `CourseOfferingGroups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);
