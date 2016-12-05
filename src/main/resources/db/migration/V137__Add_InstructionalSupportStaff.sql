CREATE TABLE `InstructionalSupportStaff` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Email` varchar(45) NOT NULL,
  `LoginId` varchar(45) NOT NULL,
  `FirstName` varchar(45) NOT NULL,
  `LastName` varchar(45) NOT NULL
  );

CREATE TABLE `InstructionalSupportAssignments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `InstructionalSupportStaffId` int(11) DEFAULT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `AppointmentType` varchar(20) NOT NULL,
  `AppointmentPercentage` int(11) NOT NULL,

  KEY `ISAssignment_SectionGroupId_idx` (`SectionGroupId`),
  KEY `ISAssignment_InstructionalSupportStaffId_idx` (`InstructionalSupportStaffId`),
  CONSTRAINT `ISAssignments_InstructionalSupportStaffId` FOREIGN KEY (`InstructionalSupportStaffId`) REFERENCES `InstructionalSupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `ISAssignments_SectionGroupId` FOREIGN KEY (`SectionGroupId`) REFERENCES `SectionGroups` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `StudentInstructionalSupportPreferences` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `InstructionalSupportStaffId` int(11) NOT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `StudentInstructionalSupportCallId` int(11) NOT NULL,
  `Comment` varchar(20) DEFAULT NULL,
  `Type` varchar(20) NOT NULL,
  `Priority` int(11) NOT NULL,
  KEY `StudentISCPreferences_SectionGroupId_idx` (`SectionGroupId`),
  KEY `StudentISCPreferences_InstructionalSupportStaffId_idx` (`InstructionalSupportStaffId`),
  CONSTRAINT `StudentISCPreferences_InstructionalSupportStaffId` FOREIGN KEY (`InstructionalSupportStaffId`) REFERENCES `InstructionalSupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `StudentISCPreferences_SectionGroupId` FOREIGN KEY (`SectionGroupId`) REFERENCES `SectionGroups` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `InstructorInstructionalSupportPreferences` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `InstructorId` int(11) NOT NULL,
  `InstructionalSupportStaffId` int(11) NOT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `InstructorInstructionalSupportCallId` int(11) NOT NULL,
  `Priority` int(11) NOT NULL,
  KEY `InstructorISCPreferences_SectionGroupId_idx` (`SectionGroupId`),
  KEY `InstructorISCPreferences_InstructionalSupportStaffId_idx` (`InstructionalSupportStaffId`),
  CONSTRAINT `InstructorISCPreferences_InstructionalSupportStaffId` FOREIGN KEY (`InstructionalSupportStaffId`) REFERENCES `InstructionalSupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `InstructorISCPreferences_SectionGroupId` FOREIGN KEY (`SectionGroupId`) REFERENCES `SectionGroups` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `StudentInstructionalSupportCalls` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `ScheduleId` int(11) NOT NULL,
  `StartDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DueDate` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `Message` text,
  `SendEmails` tinyint(1) NOT NULL DEFAULT '0',
  `AllowSubmissionAfterDueDate` tinyint(1) NOT NULL DEFAULT '1',
  `TermCode` varchar(6) NOT NULL,
  `MinimumNumberOfPreferences` int(11) NOT NULL DEFAULT 0,
  `CollectGeneralComments` tinyint(1) NOT NULL DEFAULT '1',
  `CollectTeachingQualifications` tinyint(1) NOT NULL DEFAULT '1',
  `CollectPreferenceComments` tinyint(1) NOT NULL DEFAULT '1',
  `CollectEligibilityConfirmation` tinyint(1) NOT NULL DEFAULT '1',
  `CollectTeachingAssistantPreferences` tinyint(1) NOT NULL DEFAULT '1',
  `CollectReaderPreferences` tinyint(1) NOT NULL DEFAULT '1',
  `CollectAssociateInstructorPreferences` tinyint(1) NOT NULL DEFAULT '1',
  KEY `StudentInstructionalSupportCalls_ScheduleId_idx` (`ScheduleId`),
  CONSTRAINT `StudentInstructionalSupportCalls_ScheduleId` FOREIGN KEY (`ScheduleId`) REFERENCES `Schedules` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);


CREATE TABLE `InstructorInstructionalSupportCalls` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `ScheduleId` int(11) NOT NULL,
  `StartDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DueDate` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `Message` text,
  `SendEmails` tinyint(1) NOT NULL DEFAULT '0',
  `AllowSubmissionAfterDueDate` tinyint(1) NOT NULL DEFAULT '1',
  `TermCode` varchar(6) NOT NULL,
  KEY `InstructorInstructionalSupportCalls_ScheduleId_idx` (`ScheduleId`),
  CONSTRAINT `InstructorInstructionalSupportCalls_ScheduleId` FOREIGN KEY (`ScheduleId`) REFERENCES `Schedules` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);


CREATE TABLE `StudentInstructionalSupportCallResponses` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `NotifiedAt` timestamp NULL DEFAULT NULL,
  `WarnedAt` timestamp NULL DEFAULT NULL,
  `Submitted` tinyint(1) NOT NULL DEFAULT '0',
  `StudentInstructionalSupportCallId` int(11) NOT NULL,
  `InstructionalSupportStaffId` int(11) NOT NULL,
  `GeneralComments` text,
  `TeachingQualifications` text,
  KEY `StudentISCResponses_InstructionalSupportStaffId_idx` (`InstructionalSupportStaffId`),
  CONSTRAINT `StudentISCResponses_InstructionalSupportStaffId` FOREIGN KEY (`InstructionalSupportStaffId`) REFERENCES `InstructionalSupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `InstructorInstructionalSupportCallResponses` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `NotifiedAt` timestamp NULL DEFAULT NULL,
  `WarnedAt` timestamp NULL DEFAULT NULL,
  `Submitted` tinyint(1) NOT NULL DEFAULT '0',
  `InstructorInstructionalSupportCallId` int(11) NOT NULL,
  `InstructorId` int(11) NOT NULL,
  `GeneralComments` text,
  KEY `InstructorInstructionalSupportCallResponses_InstructorId_idx` (`InstructorId`),
  CONSTRAINT `InstructorInstructionalSupportCallResponses_InstructorId` FOREIGN KEY (`InstructorId`) REFERENCES `Instructors` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);