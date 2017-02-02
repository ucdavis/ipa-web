package edu.ucdavis.dss.ipa.services.jpa;


import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.InstructorInstructionalSupportCallRepository;
import edu.ucdavis.dss.ipa.services.InstructorInstructionalSupportCallResponseService;
import edu.ucdavis.dss.ipa.services.InstructorInstructionalSupportCallService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructorInstructionalSupportCallService implements InstructorInstructionalSupportCallService {

    @Inject InstructorInstructionalSupportCallRepository instructorInstructionalSupportCallRepository;
    @Inject InstructorService instructorService;
    @Inject InstructorInstructionalSupportCallResponseService instructorInstructionalSupportCallResponseService;

    @Override
    public InstructorInstructionalSupportCall findOneById(long instructorInstructionalSupportCallId) {
        return instructorInstructionalSupportCallRepository.findById(instructorInstructionalSupportCallId);
    }

    @Override
    public InstructorInstructionalSupportCall findOrCreate(InstructorInstructionalSupportCall instructorInstructionalSupportCall) {
        // Extract the supportStaffIds
        List<Long> instructorIds = new ArrayList<>();

        for (InstructorInstructionalSupportCallResponse supportCallResponse : instructorInstructionalSupportCall.getInstructorInstructionalSupportCallResponses()) {
            Long instructorId = supportCallResponse.getInstructor().getId();

            instructorIds.add(instructorId);
        }

        // Make the supportCall
        InstructorInstructionalSupportCall supportCall = this.create(instructorInstructionalSupportCall);

        // Make supportCallResponses
        List<InstructorInstructionalSupportCallResponse> supportCallResponses = new ArrayList<>();

        for (Long instructorId : instructorIds) {
            Instructor instructor = instructorService.getOneById(instructorId);
            InstructorInstructionalSupportCallResponse supportCallResponse = instructorInstructionalSupportCallResponseService.create(instructorInstructionalSupportCall, instructor);

            supportCallResponses.add(supportCallResponse);
        }

        // Tie the supportCallResponses to the new SupportCall
        instructorInstructionalSupportCall.setInstructorInstructionalSupportCallResponses(supportCallResponses);

        return instructorInstructionalSupportCall;
    }

    @Override
    public List<InstructorInstructionalSupportCall> findByScheduleId(long scheduleId) {
        return instructorInstructionalSupportCallRepository.findByScheduleId(scheduleId);
    }

    @Override
    public void delete(long instructorInstructionalSupportCallId) {
        instructorInstructionalSupportCallRepository.delete(instructorInstructionalSupportCallId);
    }

    @Override
    public List<InstructorInstructionalSupportCall> findByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
        List<InstructorInstructionalSupportCall> scheduleSupportCalls = this.findByScheduleId(scheduleId);
        List<InstructorInstructionalSupportCall> filteredSupportCalls = new ArrayList<>();

        for (InstructorInstructionalSupportCall instructorSupportCall : scheduleSupportCalls) {
            for (InstructorInstructionalSupportCallResponse instructorSupportCallResponse : instructorSupportCall.getInstructorInstructionalSupportCallResponses()) {
                if (instructorSupportCallResponse.getInstructorIdentification() == instructorId) {
                    filteredSupportCalls.add(instructorSupportCall);
                    break;
                }
            }
        }

        return filteredSupportCalls;
    }

    private InstructorInstructionalSupportCall create (InstructorInstructionalSupportCall instructorInstructionalSupportCall) {
        // Create StartDate
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        java.sql.Date sqlDate = new Date(utilDate.getTime());

        instructorInstructionalSupportCall.setStartDate(sqlDate);
        instructorInstructionalSupportCall.setInstructorInstructionalSupportCallResponses(null);

        return instructorInstructionalSupportCallRepository.save(instructorInstructionalSupportCall);
    }
}