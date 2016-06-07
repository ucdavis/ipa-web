/* Remove duplicates first ... */
DELETE e1 FROM InstructorWorkgroupRelationships e1, InstructorWorkgroupRelationships e2 WHERE e1.Workgroups_WorkgroupId = e2.Workgroups_WorkgroupId AND e1.Instructors_InstructorId = e2.Instructors_InstructorId AND e1.InstructorWorkgroupRelationshipId > e2.InstructorWorkgroupRelationshipId;

ALTER TABLE InstructorWorkgroupRelationships ADD UNIQUE KEY `uk_instructor_workgroup` (Workgroups_WorkgroupId, Instructors_InstructorId);

ALTER TABLE InstructorWorkgroupRelationships DROP COLUMN Hidden;
