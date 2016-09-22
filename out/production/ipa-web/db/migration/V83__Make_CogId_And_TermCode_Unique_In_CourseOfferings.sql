ALTER TABLE `CourseOfferings` 
ADD UNIQUE INDEX `CourseOfferingGroup_TermCode_Unique` (`CourseOfferingGroupId` ASC, `TermCode` ASC);
