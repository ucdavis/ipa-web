CREATE TABLE `WorkloadSnapshots`
(
    `Id`               INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `Name`             VARCHAR(255) NOT NULL,
    `BudgetScenarioId` INT          NOT NULL,
    `WorkgroupId`      INT          NOT NULL,
    `Year`             INT          NOT NULL,
    `CreatedAt`        timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `UpdatedAt`        timestamp    NULL     DEFAULT NULL,
    `ModifiedBy`       varchar(16)           DEFAULT NULL
);

CREATE TABLE `WorkloadAssignments`
(
    `Id`                 INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `WorkloadSnapshotId` INT          NOT NULL,
    `Year`               INT          NULL     DEFAULT NULL,
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
    `PreviousYearCensus` INT          NULL     DEFAULT NULL,
    `PlannedSeats`       INT          NULL     DEFAULT NULL,
    `StudentCreditHours` FLOAT        NULL     DEFAULT NULL,
    `CreatedAt`          timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `UpdatedAt`          timestamp    NULL     DEFAULT NULL,
    `ModifiedBy`         varchar(16)           DEFAULT NULL
);
