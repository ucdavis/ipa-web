CREATE TABLE `SchedulingNotes`(
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `UserId` int(11) NOT NULL,
  `AuthorName` varchar(100) NOT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `Message` varchar(600) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);
