package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V207__Persist_Implicit_SectionGroupCosts implements JdbcMigration {

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

		PreparedStatement psSectionGroups = connection.prepareStatement("SELECT sg.Id, sg.TeachingAssistantAppointments, sg.ReaderAppointments, sg.TermCode, c.ScheduleId FROM SectionGroups sg, Courses c WHERE sg.CourseId = c.Id;");
		connection.setAutoCommit(false);

		ResultSet rsSectionGroups = psSectionGroups.executeQuery();
		int currentSectionGroup = 0;
		rsSectionGroups.last();
		int totalSectionGroups = rsSectionGroups.getRow();
		rsSectionGroups.first();

		while(rsSectionGroups.next()) {
			currentSectionGroup += 1;

			Long sectionGroupId = rsSectionGroups.getLong("Id");
			Long scheduleId = rsSectionGroups.getLong("ScheduleId");

			// Find budgetScenarios that connect to the same schedule
			PreparedStatement psBudgetScenarios = connection.prepareStatement("SELECT bs.Id FROM BudgetScenarios bs, Budgets b WHERE bs.BudgetId = b.Id AND b.ScheduleId = ?;");
			psBudgetScenarios.setLong(1, scheduleId);
			ResultSet rsBudgetScenarios = psBudgetScenarios.executeQuery();

			rsBudgetScenarios.last();
			// Only get other resources if necessary
			Long teachingAssistantAppointments = null, readerAppointments = null, instructorId = null, instructorTypeId = null;
			int sectionCount = 0, enrollment = 0;

			if (rsBudgetScenarios.getRow() > 0) {
				teachingAssistantAppointments = rsSectionGroups.getLong("TeachingAssistantAppointments");
				readerAppointments = rsSectionGroups.getLong("ReaderAppointments");

				// Find the instructor/instructorType of record
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
				PreparedStatement psSections = connection.prepareStatement("SELECT * FROM Sections s WHERE s.SectionGroupId = ?;");
				psSections.setLong(1, sectionGroupId);
				ResultSet rsSections = psSections.executeQuery();

				rsSections.last();

				if (rsSections.getRow() > 0) {
					rsSections.first();
					sectionCount += 1;
					enrollment += rsSections.getLong("Seats");
				}
			}

			rsBudgetScenarios.first();

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
						" (BudgetScenarioId, SectionGroupId, ReaderCount, TaCount," +
						" SectionCount, Enrollment, InstructorId, InstructorTypeId)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
					);

					psCreateSectionGroupCost.setLong(1, budgetScenarioId);
					psCreateSectionGroupCost.setLong(2, sectionGroupId);
					psCreateSectionGroupCost.setLong(3, readerAppointments);
					psCreateSectionGroupCost.setLong(4, teachingAssistantAppointments);
					psCreateSectionGroupCost.setLong(5, sectionCount);
					psCreateSectionGroupCost.setLong(6, enrollment);

					if (instructorId != null) {
						psCreateSectionGroupCost.setLong(7, instructorId);
					} else {
						psCreateSectionGroupCost.setNull(7, java.sql.Types.INTEGER);
					}

					if (instructorTypeId != null) {
						psCreateSectionGroupCost.setLong(8, instructorTypeId);
					} else {
						psCreateSectionGroupCost.setNull(8, java.sql.Types.INTEGER);
					}

					psCreateSectionGroupCost.execute();
					psCreateSectionGroupCost.close();
				}
			}
		}

		// Commit changes
		connection.commit();
	}
}
