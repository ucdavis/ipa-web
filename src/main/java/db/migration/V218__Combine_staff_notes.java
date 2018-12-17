package db.migration;

import org.flywaydb.core.api.migration.jdbc.JdbcMigration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V218__Combine_staff_notes implements JdbcMigration {
    @Override
    public void migrate(Connection connection) throws Exception {
        PreparedStatement psInstructorNotes = connection.prepareStatement("SELECT * FROM InstructorNotes;");
        connection.setAutoCommit(false);

        ResultSet rsInstructorNotes = psInstructorNotes.executeQuery();

        while(rsInstructorNotes.next()) {
            Long scheduleId = rsInstructorNotes.getLong("ScheduleId");
            Long instructorId = rsInstructorNotes.getLong("InstructorId");
            String note = rsInstructorNotes.getString("Note");

            PreparedStatement psScheduleInstructorNote = connection.prepareStatement("SELECT * FROM ScheduleInstructorNotes sin WHERE sin.InstructorId = ? AND sin.ScheduleId = ?;");
            psScheduleInstructorNote.setLong(1, instructorId);
            psScheduleInstructorNote.setLong(2, scheduleId);

            ResultSet rsScheduleInstructorNote = psScheduleInstructorNote.executeQuery();
            if (rsScheduleInstructorNote.first()) {
                Long scheduleInstructorNoteId = rsScheduleInstructorNote.getLong("Id");

                String instructorComment = rsScheduleInstructorNote.getString("InstructorComment");
                instructorComment = instructorComment + System.lineSeparator() + note;
                PreparedStatement psUpdateScheduleInstructorNote = connection.prepareStatement(
                    " UPDATE ScheduleInstructorNotes" +
                        " SET InstructorComment = ?" +
                        " WHERE Id = ?;"
                );

                psUpdateScheduleInstructorNote.setString(1, instructorComment);
                psUpdateScheduleInstructorNote.setLong(2, scheduleInstructorNoteId);
                psUpdateScheduleInstructorNote.execute();
                psUpdateScheduleInstructorNote.close();
            }
        }

        // Commit changes
        connection.commit();
        System.out.println("done!");
    }
}
