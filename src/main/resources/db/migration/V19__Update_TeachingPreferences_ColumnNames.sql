ALTER TABLE TeachingPreferences
CHANGE COLUMN `InstructorId` `Instructors_InstructorId` INT NOT NULL,
CHANGE COLUMN `CourseOfferingId` `CourseOfferings_CourseOfferingId` INT NOT NULL;
