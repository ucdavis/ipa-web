CREATE TABLE `SupportStaff` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `Email` varchar(45) NOT NULL,
  `LoginId` varchar(45) NOT NULL,
  `FirstName` varchar(45) NOT NULL,
  `LastName` varchar(45) NOT NULL
  );

CREATE TABLE `SupportAssignments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `SupportStaffId` int(11) DEFAULT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `AppointmentType` varchar(20) NOT NULL,
  `AppointmentPercentage` int(11) NOT NULL,

  KEY `SupportAssignment_SectionGroupId_idx` (`SectionGroupId`),
  KEY `SupportAssignment_SupportStaffId_idx` (`SupportStaffId`),
  CONSTRAINT `SupportAssignments_SupportStaffId` FOREIGN KEY (`SupportStaffId`) REFERENCES `SupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `SupportAssignments_SectionGroupId` FOREIGN KEY (`SectionGroupId`) REFERENCES `SectionGroups` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `StudentSupportPreferences` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `SupportStaffId` int(11) NOT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `StudentSupportCallId` int(11) NOT NULL,
  `Comment` varchar(20) DEFAULT NULL,
  `Type` varchar(20) NOT NULL,
  `Priority` int(11) NOT NULL,
  KEY `StudentSupportPreferences_SectionGroupId_idx` (`SectionGroupId`),
  KEY `StudentSupportPreferences_SupportStaffId_idx` (`SupportStaffId`),
  CONSTRAINT `StudentSupportPreferences_SupportStaffId` FOREIGN KEY (`SupportStaffId`) REFERENCES `SupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `StudentSupportPreferences_SectionGroupId` FOREIGN KEY (`SectionGroupId`) REFERENCES `SectionGroups` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `InstructorSupportPreferences` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `InstructorId` int(11) NOT NULL,
  `SupportStaffId` int(11) NOT NULL,
  `SectionGroupId` int(11) NOT NULL,
  `InstructorSupportCallId` int(11) NOT NULL,
  `Priority` int(11) NOT NULL,
  KEY `InstructorSupportPreferences_SectionGroupId_idx` (`SectionGroupId`),
  KEY `InstructorSupportPreferences_SupportStaffId_idx` (`SupportStaffId`),
  CONSTRAINT `InstructorSupportPreferences_SupportStaffId` FOREIGN KEY (`SupportStaffId`) REFERENCES `SupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `InstructorSupportPreferences_SectionGroupId` FOREIGN KEY (`SectionGroupId`) REFERENCES `SectionGroups` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `StudentSupportCalls` (
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
  KEY `StudentSupportCalls_ScheduleId_idx` (`ScheduleId`),
  CONSTRAINT `StudentSupportCalls_ScheduleId` FOREIGN KEY (`ScheduleId`) REFERENCES `Schedules` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);


CREATE TABLE `InstructorSupportCalls` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `ScheduleId` int(11) NOT NULL,
  `StartDate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `DueDate` timestamp NOT NULL DEFAULT '1970-01-01 00:00:01',
  `Message` text,
  `SendEmails` tinyint(1) NOT NULL DEFAULT '0',
  `AllowSubmissionAfterDueDate` tinyint(1) NOT NULL DEFAULT '1',
  `TermCode` varchar(6) NOT NULL,
  KEY `InstructorSupportCalls_ScheduleId_idx` (`ScheduleId`),
  CONSTRAINT `InstructorSupportCalls_ScheduleId` FOREIGN KEY (`ScheduleId`) REFERENCES `Schedules` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);


CREATE TABLE `StudentSupportCallResponses` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `NotifiedAt` timestamp NULL DEFAULT NULL,
  `WarnedAt` timestamp NULL DEFAULT NULL,
  `Submitted` tinyint(1) NOT NULL DEFAULT '0',
  `StudentSupportCallId` int(11) NOT NULL,
  `SupportStaffId` int(11) NOT NULL,
  `GeneralComments` text,
  `TeachingQualifications` text,
  KEY `StudentSupportResponses_SupportStaffId_idx` (`SupportStaffId`),
  CONSTRAINT `StudentSupportResponses_InstructionalSupportStaffId` FOREIGN KEY (`SupportStaffId`) REFERENCES `SupportStaff` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);

CREATE TABLE `InstructorSupportCallResponses` (
  `Id` int(11) NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `NotifiedAt` timestamp NULL DEFAULT NULL,
  `WarnedAt` timestamp NULL DEFAULT NULL,
  `Submitted` tinyint(1) NOT NULL DEFAULT '0',
  `InstructorInstructionalSupportCallId` int(11) NOT NULL,
  `InstructorId` int(11) NOT NULL,
  `GeneralComments` text,
  KEY `InstructorSupportResponses_InstructorId_idx` (`InstructorId`),
  CONSTRAINT `InstructorSupportResponses_InstructorId` FOREIGN KEY (`InstructorId`) REFERENCES `Instructors` (`Id`) ON DELETE NO ACTION ON UPDATE NO ACTION
);