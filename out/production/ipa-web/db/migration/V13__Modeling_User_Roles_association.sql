CREATE TABLE IF NOT EXISTS  `Users_has_Roles` (
  `Users_UserId` INT NOT NULL,
  `Roles_RoleId` INT NOT NULL,
  PRIMARY KEY (`Users_UserId`, `Roles_RoleId`),
  CONSTRAINT `fk_Users_has_Roles_Users1`
    FOREIGN KEY (`Users_UserId`)
    REFERENCES  `Users` (`UserId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Users_has_Roles_Roles1`
    FOREIGN KEY (`Roles_RoleId`)
    REFERENCES  `Roles` (`RoleId`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;
