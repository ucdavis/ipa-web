/* DO NOT RENAME to data.sql - this should run after Flyway/Hibernate init and data.sql is run before Flyway init */

/* Turning off foreign key checks shouldn't be necessary. What's going on here? */
/*SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM Users;
DELETE FROM Workgroups;
DELETE FROM Locations;
DELETE FROM Schedules;
DELETE FROM Courses;
DELETE FROM SectionGroups;
DELETE FROM Sections;
DELETE FROM Activities;*/


insert into Users (Id, LoginId, LastAccessed, Email, FirstName, LastName, Token, Placeholder, UpdatedAt, CreatedAt, ModifiedBy, DisplayName, IamId) VALUES (1, 'testers', NOW(), 'testers@dss.ucdavis.edu', 'Test', 'McTesters', null, 0, NOW(), NOW(), 'system', 'Test "The Test" McTesters', 123456789);

insert into Workgroups (Id, WorkgroupName, WorkgroupCode, UpdatedAt, CreatedAt, ModifiedBy) VALUES (1, 'Department of Basketweaving', 'BAK', NOW(), NOW(), 'system');
/*
insert into Locations (Id, Description, WorkgroupId, archived) VALUES (1, 'Coffee House', 1, 0);

 insert into Schedules (Id, WorkgroupId, Year, Importing, SupportStaffSupportCallReviewOpen, InstructorSupportCallReviewOpen) VALUES (1, 1, 2017, 0, "0000000000", "0000000000");

 insert into Courses (Id, ScheduleId, Title, UnitsLow, UnitsHigh, SubjectCode, CourseNumber, EffectiveTermCode, SequencePattern) VALUES (1, 1, 'Weaving 101', 4, 4, 'BAK', '001', '198510', '001');

 insert into SectionGroups (Id, CourseId, TermCode, PlannedSeats, ShowTheStaff) VALUES (1, 1, '201703', 55, 0);

 insert into Sections (Id, Seats, Crn, SequenceNumber, SectionGroupId, Visible, CrnRestricted) VALUES (1, 50, NULL, '001', 1, NULL, NULL);

 insert into Activities (Id, SectionId, ActivityTypeCode, ActivityState, BannerLocation, BeginDate, EndDate, StartTime, EndTime, DayIndicator, Frequency, IsVirtual, LocationId, SectionGroupId, UpdatedAt, CreatedAt, ModifiedBy, SyncLocation) VALUES (1, 1, 'D', 0, '', '2016-03-28', '2016-06-02', NULL, NULL, '0000000', 1, 0, 1, 1, NULL, NULL, NULL, 1);
 */
