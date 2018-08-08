package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V205__Persist_Implicit_SectionGroupCosts implements JdbcMigration {

	/**
	 * Currently, budgetScenarios would only persist sectionGroupCosts when a user wants to override a value from the schedule,
	 * And the sectionGroupCost would only store override values, null indicating no override.
	 * This migration will ensure that all sectionGroups have a corresponding sectionGroupCost and that any null fields are updated to store a snapshot of the current value
	 * @param connection
	 * @throws Exception
	 */
	@Override
	public void migrate(Connection connection) throws Exception {
		// For each sectionGroup
		// For each budgetScenario in the same schedule as the sectionGroup
		// Query for a sectionGroupCost that matches the sectionGroupId and budgetScenarioId
		// If found, update all null fields on the sectionGroupCost with current data (from sectionGroup and course)
		// If note found, create and mirror all fields (from sectionGroup and course)


		PreparedStatement psSectionGroups = connection.prepareStatement("SELECT sg.Id, sg.TermCode, c.ScheduleId FROM SectionGroups sg, Courses c WHERE sg.CourseId = c.Id;");
		connection.setAutoCommit(false);

		ResultSet rsSectionGroups = psSectionGroups.executeQuery();

		while(rsSectionGroups.next()) {
			Long sectionGroupId = rsSectionGroups.getLong("Id");
			String termCode = rsSectionGroups.getString("TermCode");
			Long scheduleId = rsSectionGroups.getLong("ScheduleId");
			Long teachingAssistantAppointments = rsSectionGroups.getLong("TeachingAssistantAppointments");
			Long readerAppointments = rsSectionGroups.getLong("ReaderAppointments");

			// Find the instructor/instructorType of record
			Long instructorId = null;
			Long instructorTypeId = null;
			PreparedStatement psTeachingAssignments = connection.prepareStatement("SELECT * FROM TeachingAssignments t WHERE t.SectionGroupId = ?;");
			psTeachingAssignments.setLong(1, sectionGroupId);
			ResultSet rsTeachingAssignments = psTeachingAssignments.executeQuery();

			rsTeachingAssignments.last();

			if (rsTeachingAssignments.getRow() > 0) {
				rsTeachingAssignments.first();
				instructorId = rsTeachingAssignments.getLong("InstructorId");
				instructorTypeId = rsTeachingAssignments.getLong("InstructorTypeId");
			}

			// Find sectionCount/enrollment
			int sectionCount = 0;
			int enrollment = 0;
			PreparedStatement psSections = connection.prepareStatement("SELECT * FROM Sections s WHERE s.SectionGroupId = ?;");
			psSections.setLong(1, sectionGroupId);
			ResultSet rsSections = psSections.executeQuery();

			rsSections.last();

			if (rsSections.getRow() > 0) {
				rsSections.first();
				sectionCount += 1;
				enrollment += rsTeachingAssignments.getLong("Seats");
			}

			// Find budgetScenarios that connect to the same schedule
			PreparedStatement psBudgetScenarios = connection.prepareStatement("SELECT bs.Id FROM BudgetScenarios bs, Budgets b WHERE bs.BudgetId = b.Id AND b.ScheduleId = ?;");
			psBudgetScenarios.setLong(1, scheduleId);
			ResultSet rsBudgetScenarios = psBudgetScenarios.executeQuery();

			while(rsBudgetScenarios.next()) {
				Long budgetScenarioId = rsSectionGroups.getLong("Id");

				PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts sgc WHERE sgc.BudgetScenarioId = ? AND sgc.SectionGroupId = ?;");
				psSectionGroupCosts.setLong(1, budgetScenarioId);
				psSectionGroupCosts.setLong(2, sectionGroupId);
				ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();

				rsSectionGroupCosts.last();
				int rowCount = rsSectionGroupCosts.getRow();
				rsSectionGroupCosts.first();

				if (rowCount > 0) {
					while(rsSectionGroupCosts.next()) {
						Long sectionGroupCostId = rsSectionGroupCosts.getLong("Id");
						Long overrideReaderAppointments = rsSectionGroupCosts.getLong("ReaderCount");
						Long overrideTaAppointments = rsSectionGroupCosts.getLong("TaCount");
						Long overrideInstructorId = rsSectionGroupCosts.getLong("InstructorId");
						Long overrideInstructorTypeId = rsSectionGroupCosts.getLong("InstructorTypeId");
						Long overrideSectionCount = rsSectionGroupCosts.getLong("SectionCount");
						Long overrideEnrollment = rsSectionGroupCosts.getLong("Enrollment");

						PreparedStatement psUpdateSectionGroupCost = connection.prepareStatement(
							" UPDATE `SectionGroupCosts`" +
							" SET `BudgetScenarioId` = ?," +
							" SET `SectionGroupId` = ?," +
							" SET `ReaderAppointments` = ?," +
							" SET `TeachingAssistantAppointments` = ?," +
							" SET `InstructorId` = ?," +
							" SET `InstructorTypeId` = ?," +
							" SET `SectionCount` = ?," +
							" SET `Enrollment` = ?," +
							" WHERE `Id` = ?;"
						);

						psUpdateSectionGroupCost.setLong(1, budgetScenarioId);
						psUpdateSectionGroupCost.setLong(2, sectionGroupId);
						psUpdateSectionGroupCost.setLong(3, overrideReaderAppointments != null ? overrideReaderAppointments : readerAppointments);
						psUpdateSectionGroupCost.setLong(4, overrideTaAppointments != null ? overrideTaAppointments : teachingAssistantAppointments);
						psUpdateSectionGroupCost.setLong(5, overrideInstructorId != null ? overrideInstructorId : instructorId);
						psUpdateSectionGroupCost.setLong(6, overrideInstructorTypeId != null ? overrideInstructorTypeId : instructorTypeId);
						psUpdateSectionGroupCost.setLong(7, overrideSectionCount != null ? overrideSectionCount : sectionCount);
						psUpdateSectionGroupCost.setLong(8, overrideEnrollment != null ? overrideEnrollment : enrollment);
						psUpdateSectionGroupCost.setLong(9, sectionGroupCostId);

						psUpdateSectionGroupCost.execute();
						psUpdateSectionGroupCost.close();
					}
				} else {
					PreparedStatement psCreateSectionGroupCost = connection.prepareStatement(
						" INSERT INTO `SectionGroupCosts`" +
						" (BudgetScenarioId, SectionGroupId, ReaderAppointments, TeachingAssistantAppointments," +
						" InstructorId, InstructorTypeId, SectionCount, Enrollment)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
					);

					psCreateSectionGroupCost.setLong(1, budgetScenarioId);
					psCreateSectionGroupCost.setLong(2, sectionGroupId);
					psCreateSectionGroupCost.setLong(3, readerAppointments);
					psCreateSectionGroupCost.setLong(4, teachingAssistantAppointments);
					psCreateSectionGroupCost.setLong(5, instructorId);
					psCreateSectionGroupCost.setLong(6, instructorTypeId);
					psCreateSectionGroupCost.setLong(7, sectionCount);
					psCreateSectionGroupCost.setLong(8, enrollment);

					psCreateSectionGroupCost.execute();
					psCreateSectionGroupCost.close();
				}
			}
		}

		// Commit changes
		connection.commit();
	}
}
