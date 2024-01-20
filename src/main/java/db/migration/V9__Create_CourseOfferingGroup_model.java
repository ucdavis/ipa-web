package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V9__Create_CourseOfferingGroup_model extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();

		// Create the CourseOfferingGroups table
		String createCOG = "CREATE TABLE IF NOT EXISTS `CourseOfferingGroups` ("
				+ "`id` int(11) unsigned NOT NULL AUTO_INCREMENT,"
				+ "`ScheduleId` INT NOT NULL,"
				+ "PRIMARY KEY (`id`)"
				+ ") ENGINE=InnoDB;";
		PreparedStatement stCreateCOG = connection.prepareStatement(createCOG);
		stCreateCOG.execute();
		stCreateCOG.close();

		// Add CourseOfferingGroupId to CourseOfferings
		String addCOGIdToCOs = "ALTER TABLE `CourseOfferings`  ADD COLUMN `CourseOfferingGroupId` INT(11) NULL;";
		PreparedStatement stAddCOGIdToCOs = connection.prepareStatement(addCOGIdToCOs);
		stAddCOGIdToCOs.execute();
		stAddCOGIdToCOs.close();


		String schedules = "SELECT * FROM Schedules";
		PreparedStatement stSchedules = connection.prepareStatement(schedules);

		try {
			ResultSet rsSchedules = stSchedules.executeQuery(schedules);
			// Loop over schedules
			while (rsSchedules.next()) {
				int scheduleId = rsSchedules.getInt("ScheduleId");

				// Get the schedule courseOfferings
				String courseOfferings1 = "SELECT * FROM CourseOfferings WHERE ScheduleId = " + scheduleId + ";";
				PreparedStatement stCourseOfferings1 = connection.prepareStatement(courseOfferings1);
				ResultSet rsCourseOfferings1 = stCourseOfferings1.executeQuery(courseOfferings1);

				// Create an array to indicate which COs were already processed
				List<Integer> processedCOs = new ArrayList<Integer>(); 

				while (rsCourseOfferings1.next()) {
					Integer coId1 = rsCourseOfferings1.getInt("CourseOfferingId");
					Integer courseId1 = rsCourseOfferings1.getInt("CourseId");

					if (!processedCOs.contains(coId1)) {

						processedCOs.add(coId1);
						// Create a new group entry in CourseOfferingGroups table
						String newCOG = "INSERT INTO `CourseOfferingGroups` (`ScheduleId`) VALUES (" + scheduleId + ");";
						PreparedStatement stNewCOG = connection.prepareStatement(newCOG, Statement.RETURN_GENERATED_KEYS);
						stNewCOG.execute();

						// Get the Id of the newly generated COG
						ResultSet generatedCOGKeys = stNewCOG.getGeneratedKeys();
						if (generatedCOGKeys.next()) {
							int cogId = generatedCOGKeys.getInt(1);

							// associate this Co to the entry we just created
							String associateCo1 = "UPDATE `CourseOfferings` SET `CourseOfferingGroupId` = " + cogId + " WHERE CourseOfferingId = " + coId1 + ";";
							PreparedStatement stAssociateCO1 = connection.prepareStatement(associateCo1);
							stAssociateCO1.execute();
							stAssociateCO1.close();

							// Get the courseOffering Sections
							String sections1 = "SELECT * FROM Sections WHERE CourseOfferings_CourseOfferingId = " + coId1 + ";";
							PreparedStatement stSections1 = connection.prepareStatement(sections1);
							ResultSet rsSections1 = stSections1.executeQuery(sections1);

							if (rsSections1.next()) {
								// Get the rest of the schedule courseOfferings
								String courseOfferings2 = "SELECT * FROM CourseOfferings WHERE ScheduleId = " + scheduleId + ";";
								PreparedStatement stCourseOfferings2 = connection.prepareStatement(courseOfferings2);
								ResultSet rsCourseOfferings2 = stCourseOfferings2.executeQuery(courseOfferings2);

								while (rsCourseOfferings2.next()) {
									Integer coId2 = rsCourseOfferings2.getInt("CourseOfferingId");
									Integer courseId2 = rsCourseOfferings2.getInt("CourseId");

									if (courseId1 == courseId2) {
	
										// Get the courseOffering Sections
										String sections2 = "SELECT * FROM Sections WHERE CourseOfferings_CourseOfferingId = " + coId2 + ";";
										PreparedStatement stSections2 = connection.prepareStatement(sections2);
										ResultSet rsSections2 = stSections2.executeQuery(sections2);
	
										if (rsSections2.next()) {
											String sequence1 = rsSections1.getString("SequenceNumber");
											String sequence2 = rsSections2.getString("SequenceNumber");
	
											if	(
													// In the case the sequence starts with a letter and the letter matches
													(	Character.isLetter(sequence1.charAt(0))
															&&	Character.toUpperCase(sequence1.charAt(0)) == Character.toUpperCase(sequence2.charAt(0)) )
															||
															// In the case the sequence starts with a number and the whole sequence matches
															(	Character.isDigit(sequence1.charAt(0)) && sequence1.equals(sequence2) )
													) {
												processedCOs.add(coId2);
	
												// associate this Co to the same entry
												String associateCo2 = "UPDATE `CourseOfferings` SET `CourseOfferingGroupId` = " + cogId + " WHERE CourseOfferingId = " + coId2 + ";";
												PreparedStatement stAssociateCO2 = connection.prepareStatement(associateCo2);
												stAssociateCO2.execute();
												stAssociateCO2.close();
											}
										}
										rsSections2.close();
									}
								}
							}
							stSections1.close();
						}
						stNewCOG.close();

					}
				} // while (rsCourseOfferings1.next())
				stCourseOfferings1.close();
			} // while (rsSchedules.next())
			stSchedules.close();

			// Add CourseOfferingGroupId to CourseOfferings
			String removeScheduleIdFromCOs = "ALTER TABLE `CourseOfferings` DROP COLUMN `ScheduleId`;";
			PreparedStatement stRemoveScheduleIdFromCOs = connection.prepareStatement(removeScheduleIdFromCOs);
			stRemoveScheduleIdFromCOs.execute();

		} finally {
			stSchedules.close();
		}
	}

}
