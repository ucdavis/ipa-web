CREATE TABLE `SectionGroupCostInstructors`(
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `InstructorId` int(11),
  `OriginalInstructorId` int(11),
  `InstructorTypeId` int(11),
  `SectionGroupCostId` int(11) NOT NULL,
  `TeachingAssignmentId` int(11),
  `Cost` decimal(15,2),
  `Reason` varchar(30),
  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);