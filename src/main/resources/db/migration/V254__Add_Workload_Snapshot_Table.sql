CREATE TABLE `WorkloadSnapshots`
(
    `Id`               INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `Name`             VARCHAR(255) NOT NULL,
    `BudgetScenarioId` INT(11)      NOT NULL,
    `WorkgroupId`      int(11)      NOT NULL,
    `Year`             int(11)      NOT NULL,
    `CreatedAt`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `UpdatedAt`        timestamp    NULL     DEFAULT NULL,
    `ModifiedBy`       varchar(16)           DEFAULT NULL
);

CREATE TABLE `WorkloadAssignments`
(
    `Id`                 INT(11)      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `WorkloadSnapshotId` INT(11)      NOT NULL,
    `Year`               INT(11)      NULL     DEFAULT NULL,
    `Department`         VARCHAR(100) NULL     DEFAULT NULL,
    `InstructorType`     VARCHAR(100) NULL     DEFAULT NULL,
    `Name`               VARCHAR(100) NULL     DEFAULT NULL,
    `TermCode`           VARCHAR(6)   NULL     DEFAULT NULL,
    `CourseType`         VARCHAR(100) NULL     DEFAULT NULL,
    `Description`        VARCHAR(100) NULL     DEFAULT NULL,
    `Offering`           VARCHAR(100) NULL     DEFAULT NULL,
    `LastOfferedCensus`  VARCHAR(100) NULL     DEFAULT NULL,
    `Units`              VARCHAR(100) NULL     DEFAULT NULL,
    `InstructorNote`     TEXT         NULL     DEFAULT NULL,
    `Census`             VARCHAR(100) NULL     DEFAULT NULL,
    `PreviousYearCensus` INT(11)      NULL     DEFAULT NULL,
    `PlannedSeats`       INT(11)      NULL     DEFAULT NULL,
    `StudentCreditHours` FLOAT        NULL     DEFAULT NULL,
    `CreatedAt`          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `UpdatedAt`          timestamp    NULL     DEFAULT NULL,
    `ModifiedBy`         varchar(16)           DEFAULT NULL
);
