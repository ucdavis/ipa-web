CREATE TABLE `InstructorNotes` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `InstructorId` int(11) NOT NULL,
  `ScheduleId` int(11) NOT NULL,
  `Note` varchar(100) NULL,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `CreatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);
