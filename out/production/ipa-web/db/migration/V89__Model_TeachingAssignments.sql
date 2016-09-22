CREATE TABLE `TeachingAssignments` (
  `TeachingAssignmentId` INT(11) NOT NULL AUTO_INCREMENT,
  `SectionGroups_SectionGroupId` INT(11) NOT NULL,
  `Instructors_InstructorId` INT(11) NOT NULL,
  PRIMARY KEY (`TeachingAssignmentId`),
  INDEX `TeachingCallAssignments_SectionGroupId_idx` (`SectionGroups_SectionGroupId` ASC),
  INDEX `TeachingCallAssignments_InstructorId_idx` (`Instructors_InstructorId` ASC),
  CONSTRAINT `TeachingCallAssignments_SectionGroupId`
    FOREIGN KEY (`SectionGroups_SectionGroupId`)
    REFERENCES `SectionGroups` (`SectionGroupId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `TeachingCallAssignments_InstructorId`
    FOREIGN KEY (`Instructors_InstructorId`)
    REFERENCES `Instructors` (`InstructorId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION);