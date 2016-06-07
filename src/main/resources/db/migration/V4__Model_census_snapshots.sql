CREATE TABLE CensusSnapshots (
  CensusSnapshotId BIGINT UNSIGNED NOT NULL PRIMARY KEY AUTO_INCREMENT,
  SectionId INT NOT NULL,
  SnapshotCode VARCHAR(12) NOT NULL,
  CurrentAvailableSeatCount INT NOT NULL,
  CurrentEnrollmentCount INT NOT NULL,
  MaxEnrollmentCount INT NOT NULL,
  StudentCount INT NOT NULL,
  WaitCount INT NOT NULL,
  WaitCapacityCount INT NOT NULL,
  CONSTRAINT `FK_CS_SECTION` FOREIGN KEY (`SectionId`) REFERENCES `Sections` (`SectionId`)
);
