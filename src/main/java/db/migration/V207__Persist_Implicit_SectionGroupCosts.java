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
			Long teachingAssistantAppointments = null, readerAppointments = null, instructorId = null, instructorTypeId = null, sectionCount = null, enrollment = null;

			if (rsBudgetScenarios.getRow() > 0) {
				teachingAssistantAppointments = rsSectionGroups.getLong("TeachingAssistantAppointments");
				teachingAssistantAppointments = rsSectionGroups.wasNull() == false ? teachingAssistantAppointments : null;

				readerAppointments = rsSectionGroups.getLong("ReaderAppointments");
				readerAppointments = rsSectionGroups.wasNull() == false ? readerAppointments : null;

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



				while(rsSections.next()) {
					if (sectionCount == null) { sectionCount = 0L; }
					if (enrollment == null) { enrollment = 0L; }

					sectionCount += 1;
					enrollment += rsSections.getLong("Seats");
				}
			}

			rsBudgetScenarios.first();

			while(rsBudgetScenarios.next()) {
				Long budgetScenarioId = rsBudgetScenarios.getLong("Id");

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
							" SET `ReaderCount` = ?," +
							" SET `TaCount` = ?," +
							" SET `InstructorId` = ?," +
							" SET `InstructorTypeId` = ?," +
							" SET `SectionCount` = ?," +
							" SET `Enrollment` = ?," +
							" WHERE `Id` = ?;"
						);

						psUpdateSectionGroupCost.setLong(1, budgetScenarioId);
						psUpdateSectionGroupCost.setLong(2, sectionGroupId);

						Long readers = overrideReaderAppointments != null ? overrideReaderAppointments : readerAppointments;

						if (readers != null) {
							psUpdateSectionGroupCost.setLong(3, readers);
						} else {
							psUpdateSectionGroupCost.setNull(3, java.sql.Types.FLOAT);
						}

						Long tas = overrideTaAppointments != null ? overrideTaAppointments : teachingAssistantAppointments;

						if (tas != null) {
							psUpdateSectionGroupCost.setFloat(4, tas);
						} else {
							psUpdateSectionGroupCost.setNull(4, java.sql.Types.FLOAT);
						}

						Long _instructorId = overrideInstructorId != null ? overrideInstructorId : instructorId;

						if (_instructorId != null) {
							psUpdateSectionGroupCost.setFloat(5, _instructorId);
						} else {
							psUpdateSectionGroupCost.setNull(5, java.sql.Types.FLOAT);
						}

						Long _instructorTypeId = overrideInstructorTypeId != null ? overrideInstructorTypeId : instructorTypeId;

						if (_instructorTypeId != null) {
							psUpdateSectionGroupCost.setFloat(6, _instructorTypeId);
						} else {
							psUpdateSectionGroupCost.setNull(6, java.sql.Types.FLOAT);
						}

						Long _sectionCount = overrideSectionCount != null ? overrideSectionCount : sectionCount;

						if (_sectionCount != null) {
							psUpdateSectionGroupCost.setLong(7, instructorId);
						} else {
							psUpdateSectionGroupCost.setNull(7, java.sql.Types.INTEGER);
						}

						Long _enrollment = overrideEnrollment != null ? overrideEnrollment : enrollment;

						if (_enrollment != null) {
							psUpdateSectionGroupCost.setLong(8, _enrollment);
						} else {
							psUpdateSectionGroupCost.setNull(8, java.sql.Types.INTEGER);
						}

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
