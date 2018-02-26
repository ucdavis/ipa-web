/* Ensure database is empty. Flyway will re-create the schema. */
SET FOREIGN_KEY_CHECKS = 0;
drop table if exists Activities;
drop table if exists ActivityLog;
drop table if exists ActivityLogTag;
drop table if exists BudgetScenarios;
drop table if exists Budgets;
drop table if exists Courses;
drop table if exists Courses_has_Tags;
drop table if exists InstructorCosts;
drop table if exists InstructorSupportCallResponses;
drop table if exists InstructorSupportPreferences;
drop table if exists Instructors;
drop table if exists LineItemCategories;
drop table if exists LineItemComments;
drop table if exists LineItems;
drop table if exists Locations;
drop table if exists Notices;
drop table if exists Roles;
drop table if exists ScheduleInstructorNotes;
drop table if exists Schedules;
drop table if exists SectionGroupCostComments;
drop table if exists SectionGroupCosts;
drop table if exists SectionGroups;
drop table if exists Sections;
drop table if exists StudentSupportCallResponses;
drop table if exists StudentSupportPreferences;
drop table if exists SupportAssignments;
drop table if exists SupportAppointments;
drop table if exists SupportStaff;
drop table if exists SyncActions;
drop table if exists Tags;
drop table if exists TeachingAssignments;
drop table if exists TeachingCallReceipts;
drop table if exists TeachingCallResponses;
drop table if exists Terms;
drop table if exists UserRoles;
drop table if exists Users;
drop table if exists Workgroups;
drop table if exists InstructorTypes;
drop table if exists schema_version;
SET FOREIGN_KEY_CHECKS = 1;
