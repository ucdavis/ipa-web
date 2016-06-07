CREATE TABLE IF NOT EXISTS  `CourseOfferingGroups_has_Tracks` (
  `CourseOfferingGroups_id` INT UNSIGNED NOT NULL,
  `Tracks_TrackId` INT NOT NULL,
  PRIMARY KEY (`CourseOfferingGroups_id`, `Tracks_TrackId`),
  CONSTRAINT `fk_CourseOfferingGroups_has_Tracks_CourseOfferingGroups`
    FOREIGN KEY (`CourseOfferingGroups_id`)
    REFERENCES  `CourseOfferingGroups` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_CourseOfferingGroups_has_Tracks_Tracks1`
    FOREIGN KEY (`Tracks_TrackId`)
    REFERENCES  `Tracks` (`TrackId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);