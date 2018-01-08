CREATE TABLE `SupportAppointments` (
  `Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `SupportStaffId` int(11) NOT NULL,
  `ScheduleId` int(11) NOT NULL,
  `Percentage` FLOAT NULL,
  `Type` VARCHAR(50) NOT NULL,
  `TermCode` VARCHAR(6) NOT NULL
);