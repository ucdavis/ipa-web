ALTER TABLE `TeachingCallResponses`
DROP INDEX `TeachingCall_Instructor_TermCode`,
ADD UNIQUE INDEX `Schedule_Instructor_TermCode` (`Schedules_ScheduleId` ASC, `TermCode` ASC, `InstructorId` ASC);