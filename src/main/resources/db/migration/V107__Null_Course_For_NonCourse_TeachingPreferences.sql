-- Fixes inconsistent data described in #1183

UPDATE TeachingPreferences
SET CourseOfferings_CourseOfferingId = NULL,
	Courses_CourseId = NULL
WHERE isBuyout = 1
OR isCourseRelease = 1
OR isSabbatical = 1;
