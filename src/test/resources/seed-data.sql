/* DO NOT RENAME to data.sql - this should run after Flyway/Hibernate init and data.sql is run before Flyway init */

insert into Users (Id, LoginId, LastAccessed, Email, FirstName, LastName, Token, Placeholder, UpdatedAt, CreatedAt, ModifiedBy, DisplayName, IamId) VALUES (1, 'testers', NOW(), 'testers@dss.ucdavis.edu', 'Test', 'McTesters', null, 0, NOW(), NOW(), 'system', 'Test "The Test" McTesters', 123456789);

insert into Workgroups (Id, WorkgroupName, WorkgroupCode, UpdatedAt, CreatedAt, ModifiedBy) VALUES (1, 'Department of Basketweaving', 'BAK', NOW(), NOW(), 'system');
