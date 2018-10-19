package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V208__Persist_Implicit_SectionGroupCosts implements JdbcMigration {

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

		while(rsSectionGroups.next()) {
			Long sectionGroupId = rsSectionGroups.getLong("Id");
			Long scheduleId = rsSectionGroups.getLong("ScheduleId");

			// Find budgetScenarios that connect to the same schedule
			PreparedStatement psBudgetScenarios = connection.prepareStatement("SELECT bs.Id FROM BudgetScenarios bs, Budgets b WHERE bs.BudgetId = b.Id AND b.ScheduleId = ?;");
			psBudgetScenarios.setLong(1, scheduleId);
			ResultSet rsBudgetScenarios = psBudgetScenarios.executeQuery();
			rsBudgetScenarios.last();
			int budgetScenarioCount = rsBudgetScenarios.getRow();
			rsBudgetScenarios.beforeFirst();
			// Only get other resources if necessary
			Long teachingAssistantAppointments = null, readerAppointments = null, instructorId = null, instructorTypeId = null, sectionCount = null, enrollment = null;

			if (budgetScenarioCount > 0) {
				teachingAssistantAppointments = rsSectionGroups.getLong("TeachingAssistantAppointments");
				teachingAssistantAppointments = rsSectionGroups.wasNull() ? null : teachingAssistantAppointments;

				readerAppointments = rsSectionGroups.getLong("ReaderAppointments");
				readerAppointments = rsSectionGroups.wasNull() ? null : readerAppointments;

				// Find the instructor/instructorType of record
				PreparedStatement psTeachingAssignments = connection.prepareStatement("SELECT * FROM TeachingAssignments t WHERE t.SectionGroupId = ? AND t.Approved = 1;");
				psTeachingAssignments.setLong(1, sectionGroupId);
				ResultSet rsTeachingAssignments = psTeachingAssignments.executeQuery();

				rsTeachingAssignments.last();

				if (rsTeachingAssignments.getRow() > 0) {
					rsTeachingAssignments.first();
					instructorId = rsTeachingAssignments.getLong("InstructorId");
					instructorId = rsTeachingAssignments.wasNull() ? null : instructorId;

					instructorTypeId = rsTeachingAssignments.getLong("InstructorTypeId");
					instructorTypeId = rsTeachingAssignments.wasNull() ? null : instructorTypeId;
				}

				// Find sectionCount/enrollment
				PreparedStatement psSections = connection.prepareStatement("SELECT * FROM Sections s WHERE s.SectionGroupId = ?;");
				psSections.setLong(1, sectionGroupId);
				ResultSet rsSections = psSections.executeQuery();

				while(rsSections.next()) {
					if (sectionCount == null) { sectionCount = 0L; }
					if (enrollment == null) { enrollment = 0L; }

					sectionCount += 1;
					enrollment += rsSections.getLong("Seats");
				}
			}

			while(rsBudgetScenarios.next()) {
				// Found a specific budgetScenario relevant to the sectionGroup
				Long budgetScenarioId = rsBudgetScenarios.getLong("Id");

				PreparedStatement psSectionGroupCosts = connection.prepareStatement("SELECT * FROM SectionGroupCosts sgc WHERE sgc.BudgetScenarioId = ? AND sgc.SectionGroupId = ?;");
				psSectionGroupCosts.setLong(1, budgetScenarioId);
				psSectionGroupCosts.setLong(2, sectionGroupId);
				ResultSet rsSectionGroupCosts = psSectionGroupCosts.executeQuery();

				rsSectionGroupCosts.last();
				int rowCount = rsSectionGroupCosts.getRow();
				rsSectionGroupCosts.beforeFirst();

				// Found the sectionGroupCost that matched the unique scenarioId/sectionGroupId pair
				if (rowCount > 0) {
					rsSectionGroupCosts.next();

					Long sectionGroupCostId = rsSectionGroupCosts.getLong("Id");
					Long originalReaderAppointments = rsSectionGroupCosts.getLong("ReaderCount");
					originalReaderAppointments = rsSectionGroupCosts.wasNull() ? null : originalReaderAppointments;

					Long originalTaAppointments = rsSectionGroupCosts.getLong("TaCount");
					originalTaAppointments = rsSectionGroupCosts.wasNull() ? null : originalTaAppointments;

					Long originalInstructorId = rsSectionGroupCosts.getLong("InstructorId");
					originalInstructorId = rsSectionGroupCosts.wasNull() ? null : originalInstructorId;

					Long originalInstructorTypeId = rsSectionGroupCosts.getLong("InstructorTypeId");
					originalInstructorTypeId = rsSectionGroupCosts.wasNull() ? null : originalInstructorTypeId;

					Long originalSectionCount = rsSectionGroupCosts.getLong("SectionCount");
					originalSectionCount = rsSectionGroupCosts.wasNull() ? null : originalSectionCount;

					Long originalEnrollment = rsSectionGroupCosts.getLong("Enrollment");
					originalEnrollment = rsSectionGroupCosts.wasNull() ? null : originalEnrollment;

					PreparedStatement psUpdateSectionGroupCost = connection.prepareStatement(
						" UPDATE SectionGroupCosts" +
						" SET BudgetScenarioId = ?," +
						" SectionGroupId = ?," +
						" ReaderCount = ?," +
						" TaCount = ?," +
						" InstructorId = ?," +
						" InstructorTypeId = ?," +
						" SectionCount = ?," +
						" Enrollment = ?" +
						" WHERE Id = ?;"
					);

					psUpdateSectionGroupCost.setLong(1, budgetScenarioId);
					psUpdateSectionGroupCost.setLong(2, sectionGroupId);

					Long calculatedReaders = originalReaderAppointments != null ? originalReaderAppointments : readerAppointments;

					if (calculatedReaders != null) {
						psUpdateSectionGroupCost.setLong(3, calculatedReaders);
					} else {
						psUpdateSectionGroupCost.setNull(3, java.sql.Types.FLOAT);
					}

					Long calculatedTAs = originalTaAppointments != null ? originalTaAppointments : teachingAssistantAppointments;

					if (calculatedTAs != null) {
						psUpdateSectionGroupCost.setLong(4, calculatedTAs);
					} else {
						psUpdateSectionGroupCost.setNull(4, java.sql.Types.FLOAT);
					}

					Long calculatedInstructorId = originalInstructorId != null ? originalInstructorId : instructorId;

					if (calculatedInstructorId != null) {
						psUpdateSectionGroupCost.setLong(5, calculatedInstructorId);
					} else {
						psUpdateSectionGroupCost.setNull(5, java.sql.Types.INTEGER);
					}

					Long calculatedInstructorTypeId = originalInstructorTypeId != null ? originalInstructorTypeId : instructorTypeId;

					if (calculatedInstructorTypeId != null) {
						psUpdateSectionGroupCost.setLong(6, calculatedInstructorTypeId);
					} else {
						psUpdateSectionGroupCost.setNull(6, java.sql.Types.INTEGER);
					}

					Long calculatedSectionCount = originalSectionCount != null ? originalSectionCount : sectionCount;

					if (calculatedSectionCount != null) {
						psUpdateSectionGroupCost.setLong(7, calculatedSectionCount);
					} else {
						psUpdateSectionGroupCost.setNull(7, java.sql.Types.INTEGER);
					}

					Long calculatedEnrollment = originalEnrollment != null ? originalEnrollment : enrollment;

					if (calculatedEnrollment != null) {
						psUpdateSectionGroupCost.setLong(8, calculatedEnrollment);
					} else {
						psUpdateSectionGroupCost.setNull(8, java.sql.Types.INTEGER);
					}

					psUpdateSectionGroupCost.setLong(9, sectionGroupCostId);

					psUpdateSectionGroupCost.execute();
					psUpdateSectionGroupCost.close();
				}
				// Make new sectionGroupCost instead
				else {
					PreparedStatement psCreateSectionGroupCost = connection.prepareStatement(
						" INSERT INTO `SectionGroupCosts`" +
						" (BudgetScenarioId, SectionGroupId, ReaderCount, TaCount," +
						" SectionCount, Enrollment, InstructorId, InstructorTypeId)" +
						" VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
					);

					psCreateSectionGroupCost.setLong(1, budgetScenarioId);
					psCreateSectionGroupCost.setLong(2, sectionGroupId);

					if (readerAppointments != null) {
						psCreateSectionGroupCost.setFloat(3, readerAppointments);
					} else {
						psCreateSectionGroupCost.setNull(3, java.sql.Types.FLOAT);
					}

					if (teachingAssistantAppointments != null) {
						psCreateSectionGroupCost.setFloat(4, teachingAssistantAppointments);
					} else {
						psCreateSectionGroupCost.setNull(4, java.sql.Types.FLOAT);
					}

					if (sectionCount != null) {
						psCreateSectionGroupCost.setFloat(5, sectionCount);
					} else {
						psCreateSectionGroupCost.setNull(5, java.sql.Types.FLOAT);
					}

					if (enrollment != null) {
						psCreateSectionGroupCost.setFloat(6, enrollment);
					} else {
						psCreateSectionGroupCost.setNull(6, java.sql.Types.FLOAT);
					}

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
