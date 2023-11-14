package db.migration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;


public class V95__Move_Comment_From_TeachingCallResponses_To_TeachingCallReceipts extends BaseJavaMigration {

	@Override
	public void migrate(Context context) throws Exception {
		Connection connection = context.getConnection();

		// Add the Comment column to TeachingCallReceipts
		String createColumnQuery = "ALTER TABLE `TeachingCallReceipts`"
				+ " ADD COLUMN `Comment` TEXT NULL DEFAULT NULL;";
		PreparedStatement createColumnStatement = connection.prepareStatement(createColumnQuery);

		// Combine TeachingCallResponses comments
		PreparedStatement combinedCommentsQuery = connection.prepareStatement(
				"SELECT"
				+ "  responses.`Instructors_InstructorId` as instructorId,"
				+ "  responses.`TeachingCalls_TeachingCallId` as teachingCallId,"
				+ "  GROUP_CONCAT(responses.`Comment` SEPARATOR '.\n\n') as comments"
				+ " FROM `TeachingCallResponses` responses"
				+ " GROUP BY"
				+ "  responses.`Instructors_InstructorId`,"
				+ "  responses.`TeachingCalls_TeachingCallId`;");
		ResultSet rsCombinedComments = combinedCommentsQuery.executeQuery();
		
		try {
			createColumnStatement.execute();

			// Loop over combined comments and insert them into Receipts
			while(rsCombinedComments.next()) {
				long instructorId = rsCombinedComments.getLong("instructorId");
				long teachingCallId = rsCombinedComments.getLong("teachingCallId");
				String comments = rsCombinedComments.getString("comments");
				
				// Save combine comments in TeachingCallReceipts
				String updateTeachingCallReceiptsQuery = "UPDATE `TeachingCallReceipts`"
						+ " SET Comment = ?"
						+ " WHERE TeachingCalls_TeachingCallId = ?"
						+ " AND Instructors_InstructorId = ?";
				PreparedStatement updateTeachingCallReceiptsStatement = connection.prepareStatement(updateTeachingCallReceiptsQuery);
				updateTeachingCallReceiptsStatement.setString(1, comments);
				updateTeachingCallReceiptsStatement.setLong(2, teachingCallId);
				updateTeachingCallReceiptsStatement.setLong(3, instructorId);
				
				updateTeachingCallReceiptsStatement.executeUpdate();
				updateTeachingCallReceiptsStatement.close();
			}

			// Drop the Comment column from TeachingCallResponses
			String dropColumnQuery = "ALTER TABLE `TeachingCallResponses` DROP COLUMN `Comment`;";
			PreparedStatement dropColumnStatement = connection.prepareStatement(dropColumnQuery);
			dropColumnStatement.execute();
			dropColumnStatement.close();

		} finally {
			createColumnStatement.close();
			rsCombinedComments.close();
		}
	}

}
