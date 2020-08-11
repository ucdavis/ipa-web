CREATE TABLE `CourseComments`(
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `UserId` int(11) NOT NULL,
  `AuthorName` varchar(100) NOT NULL,
  `CourseId` int(11) NOT NULL,
  `Comment` varchar(600) NOT NULL,
  `CreatedAt` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `UpdatedAt` timestamp NULL DEFAULT NULL,
  `ModifiedBy` varchar(16) DEFAULT NULL
);