/* Remove userRoles based on the federationInstructor role of 8 */
DELETE FROM `UserRoles` WHERE RoleId = 8;

/* Remove userRoles based on the senateInstructor role of 1 */
DELETE FROM `UserRoles` WHERE RoleId = 1;

/* Remove userRoles based on the lecturer role of 14 */
DELETE FROM `UserRoles` WHERE RoleId = 14;

DELETE FROM `Roles` WHERE Name = 'senateInstructor';
DELETE FROM `Roles` WHERE Name = 'federationInstructor';
DELETE FROM `Roles` WHERE Name = 'lecturer';
