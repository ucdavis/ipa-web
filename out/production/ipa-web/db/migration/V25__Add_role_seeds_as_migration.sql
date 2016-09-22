/* Role tokens need to be unique */
ALTER TABLE `Roles` ADD CONSTRAINT ROLES_UNIQUE_NAME UNIQUE(name);

/* Role seed data current as of 4-8-2015 */
INSERT INTO `Roles` (`Name`) VALUES ('instructor');
INSERT INTO `Roles` (`Name`) VALUES ('academicCoordinator');
INSERT INTO `Roles` (`Name`) VALUES ('admin');
