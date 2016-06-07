ALTER TABLE `TeachingPreferences`
ADD UNIQUE INDEX `idx_unique_course_instructor` (`Courses_CourseId`, `Instructors_InstructorId`);
