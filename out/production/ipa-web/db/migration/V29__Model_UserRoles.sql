CREATE TABLE IF NOT EXISTS  `UserRoles` (
	`UserRoleId` INT NOT NULL AUTO_INCREMENT,
	`Users_UserId` INT NOT NULL,
	`Departments_DepartmentId` INT NOT NULL,
	`Roles_RoleId` INT NOT NULL,
  PRIMARY KEY (`UserRoleId`))
ENGINE = InnoDB;