DROP TABLE `LineItemComments`;
DROP TABLE `SectionGroupCostComments`;

CREATE TABLE `LineItemComments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Comment` varchar(300) NOT NULL,
  `UserId` int(11) NOT NULL,
  `AuthorName` varchar(50) NOT NULL,
  `LineItemId` int(11) NOT NULL
);

CREATE TABLE `SectionGroupCostComments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Comment` varchar(300) NOT NULL,
  `UserId` int(11) NOT NULL,
  `AuthorName` varchar(50) NOT NULL,
  `SectionGroupCostId` int(11) NOT NULL
);