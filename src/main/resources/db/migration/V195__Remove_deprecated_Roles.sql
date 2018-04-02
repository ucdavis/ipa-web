/* Remove userRoles based on the federationInstructor role */
DELETE FROM `UserRoles` WHERE RoleId = 8;

/* Remove userRoles based on the senateInstructor role */
DELETE FROM `UserRoles` WHERE RoleId = 1;

/* Remove userRoles based on the lecturer role */
DELETE FROM `UserRoles` WHERE RoleId = 14;

DELETE FROM `Roles` WHERE Name = 'senateInstructor';
DELETE FROM `Roles` WHERE Name = 'federationInstructor';
DELETE FROM `Roles` WHERE Name = 'lecturer';
