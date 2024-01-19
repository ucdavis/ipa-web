package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V82__Update_CO_Seat_Counts extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();

		// Loop over all COs ...
		PreparedStatement fetchCOsStatement = connection.prepareStatement("SELECT CourseOfferingId FROM CourseOfferings");
		ResultSet rsFetchCos = fetchCOsStatement.executeQuery();
		
		while(rsFetchCos.next()) {
			long coId = rsFetchCos.getLong("CourseOfferingId");
			long coSeats = 0;
			
			// Fetch its SGs ...
			PreparedStatement fetchSGsStatement = connection.prepareStatement("SELECT SectionGroupId FROM `SectionGroups` WHERE CourseOfferings_CourseOfferingId=?");
			fetchSGsStatement.setLong(1, coId);
			
			ResultSet rsFetchSGs = fetchSGsStatement.executeQuery();
			while(rsFetchSGs.next()) {
				long sgId = rsFetchSGs.getLong("SectionGroupId");
				
				// Fetch the sections and add their seats up
				PreparedStatement fetchSectionsStatement = connection.prepareStatement("SELECT Seats FROM `Sections` WHERE SectionGroups_SectionGroupId=?");
				fetchSectionsStatement.setLong(1, sgId);
				
				ResultSet rsFetchSections = fetchSectionsStatement.executeQuery();
				while(rsFetchSections.next()) {
					coSeats += rsFetchSections.getLong("Seats");
				}
			}
			
			// Save the updated CO seat total ...
			String updateCOQuery = "UPDATE CourseOfferings SET SeatsTotal=? WHERE CourseOfferingId=?";
			PreparedStatement updateCOStatement = connection.prepareStatement(updateCOQuery);
			updateCOStatement.setLong(1, coSeats);
			updateCOStatement.setLong(2, coId);
			
			updateCOStatement.executeUpdate();
		}
	}

}
