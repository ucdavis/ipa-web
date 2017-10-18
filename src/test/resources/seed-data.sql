/* DO NOT RENAME to data.sql - this should run after Flyway/Hibernate init and data.sql is run before Flyway init */

DELETE FROM Users;
DELETE FROM Activities;
DELETE FROM Sections;
DELETE FROM SectionGroups;
DELETE FROM Courses;
DELETE FROM Schedules;
DELETE FROM Locations;
DELETE FROM Workgroups;

INSERT INTO Users (Id, LoginId, LastAccessed, Email, FirstName, LastName, Token, Placeholder, UpdatedAt, CreatedAt, ModifiedBy, DisplayName, IamId) VALUES (1, 'testers', NOW(), 'testers@dss.ucdavis.edu', 'Test', 'McTesters', null, 0, NOW(), NOW(), 'system', 'Test "The Test" McTesters', 123456789);
INSERT INTO Workgroups (Id, WorkgroupName, WorkgroupCode, UpdatedAt, CreatedAt, ModifiedBy) VALUES (1, 'Department of Basketweaving', 'BAK', NOW(), NOW(), 'system');
INSERT INTO Locations (Id, Description, WorkgroupId, archived) VALUES (1, 'Coffee House', 1, 0);
INSERT INTO Schedules (Id, WorkgroupId, Year, Importing, SupportStaffSupportCallReviewOpen, InstructorSupportCallReviewOpen) VALUES (1, 1, 2017, 0, "0000000000", "0000000000");

/* Numeric SectionGroup with one section, with a two activities */
INSERT INTO Courses (Id, ScheduleId, Title, UnitsLow, UnitsHigh, SubjectCode, CourseNumber, EffectiveTermCode, SequencePattern) VALUES (1, 1, 'Weaving 101', 4, 4, 'BAK', '001', '198510', '001');
INSERT INTO SectionGroups (Id, CourseId, TermCode, PlannedSeats, ShowTheStaff) VALUES (1, 1, '201703', 55, 0);
INSERT INTO Sections (Id, Seats, Crn, SequenceNumber, SectionGroupId, Visible, CrnRestricted) VALUES (1, 50, NULL, '001', 1, NULL, NULL);
INSERT INTO Activities (Id, SectionId, ActivityTypeCode, ActivityState, BannerLocation, BeginDate, EndDate, StartTime, EndTime, DayIndicator, Frequency, IsVirtual, LocationId, SectionGroupId, UpdatedAt, CreatedAt, ModifiedBy, SyncLocation) VALUES (1, NULL, 'D', 0, '', '2016-03-28', '2016-06-02', NULL, NULL, '0000000', 1, 0, 1, 1, NULL, NULL, NULL, 1);
INSERT INTO Activities (Id, SectionId, ActivityTypeCode, ActivityState, BannerLocation, BeginDate, EndDate, StartTime, EndTime, DayIndicator, Frequency, IsVirtual, LocationId, SectionGroupId, UpdatedAt, CreatedAt, ModifiedBy, SyncLocation) VALUES (2, NULL, 'L', 0, '', '2016-03-28', '2016-06-02', NULL, NULL, '0000000', 1, 0, 1, 1, NULL, NULL, NULL, 1);

/* Letter based SectionGroup with one section, with a mix of shared (sectionGroup) and section based activities */
INSERT INTO Courses (Id, ScheduleId, Title,                  UnitsLow, UnitsHigh, SubjectCode, CourseNumber, EffectiveTermCode, SequencePattern)
             VALUES (2,  1,          'Advanced Weaving 102', 4,        4,         'BAK',       '002',        '198510',          'A');
INSERT INTO SectionGroups (Id, CourseId, TermCode, PlannedSeats, ShowTheStaff)
                   VALUES (2,  2,        '201703', 34,           0);
INSERT INTO Sections (Id, Seats, Crn, SequenceNumber, SectionGroupId, Visible, CrnRestricted) VALUES (2, 50, NULL, 'A01', 2, NULL, NULL);
INSERT INTO Activities (Id, SectionId, ActivityTypeCode, ActivityState, BannerLocation, BeginDate,    EndDate,      StartTime,  EndTime,   DayIndicator, Frequency, IsVirtual, LocationId, SectionGroupId, UpdatedAt, CreatedAt, ModifiedBy, SyncLocation)
                VALUES (3,  NULL,      'L',              0,             '',             '2016-03-28', '2016-06-02', NULL,       NULL,      '0000000',    1,         0,         1,          2,              NULL,      NULL,      NULL,       1);
INSERT INTO Activities (Id, SectionId, ActivityTypeCode, ActivityState, BannerLocation, BeginDate,    EndDate,      StartTime, EndTime, DayIndicator, Frequency, IsVirtual, LocationId, SectionGroupId, UpdatedAt, CreatedAt, ModifiedBy, SyncLocation)
                VALUES (4,  2,         'D',              0,             '',             '2016-03-28', '2016-06-02', NULL,      NULL,    '0000000',    1,         0,         1,          NULL,           NULL,      NULL,      NULL,       1);
