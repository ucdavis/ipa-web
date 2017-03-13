package edu.ucdavis.dss.ipa.services.jpa;


import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorSupportCall;
import edu.ucdavis.dss.ipa.entities.InstructorSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.InstructorSupportCallRepository;
import edu.ucdavis.dss.ipa.services.InstructorSupportResponseService;
import edu.ucdavis.dss.ipa.services.InstructorSupportCallService;
import edu.ucdavis.dss.ipa.services.InstructorService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructorSupportCallService implements InstructorSupportCallService {

    @Inject
    InstructorSupportCallRepository instructorSupportCallRepository;
    @Inject InstructorService instructorService;
    @Inject
    InstructorSupportResponseService instructorSupportResponseService;

    @Override
    public InstructorSupportCall findOneById(long instructorInstructionalSupportCallId) {
        return instructorSupportCallRepository.findById(instructorInstructionalSupportCallId);
    }

    @Override
    public InstructorSupportCall findOrCreate(InstructorSupportCall instructorSupportCall) {
        // Extract the supportStaffIds
        List<Long> instructorIds = new ArrayList<>();

        for (InstructorSupportCallResponse supportCallResponse : instructorSupportCall.getInstructorSupportCallResponses()) {
            Long instructorId = supportCallResponse.getInstructor().getId();

            instructorIds.add(instructorId);
        }

        // Make the supportCall
        InstructorSupportCall supportCall = this.create(instructorSupportCall);

        // Make supportCallResponses
        List<InstructorSupportCallResponse> supportCallResponses = new ArrayList<>();

        for (Long instructorId : instructorIds) {
            Instructor instructor = instructorService.getOneById(instructorId);
            InstructorSupportCallResponse supportCallResponse = instructorSupportResponseService.create(instructorSupportCall, instructor);

            supportCallResponses.add(supportCallResponse);
        }

        // Tie the supportCallResponses to the new SupportCall
        instructorSupportCall.setInstructorSupportCallResponses(supportCallResponses);

        return instructorSupportCall;
    }

    @Override
    public List<InstructorSupportCall> findByScheduleId(long scheduleId) {
        return instructorSupportCallRepository.findByScheduleId(scheduleId);
    }

    @Override
    public void delete(long instructorInstructionalSupportCallId) {
        instructorSupportCallRepository.delete(instructorInstructionalSupportCallId);
    }

    @Override
    public List<InstructorSupportCall> findByScheduleIdAndInstructorId(long scheduleId, long instructorId) {
        List<InstructorSupportCall> scheduleSupportCalls = this.findByScheduleId(scheduleId);
        List<InstructorSupportCall> filteredSupportCalls = new ArrayList<>();

        for (InstructorSupportCall instructorSupportCall : scheduleSupportCalls) {
            for (InstructorSupportCallResponse instructorSupportCallResponse : instructorSupportCall.getInstructorSupportCallResponses()) {
                if (instructorSupportCallResponse.getInstructorIdentification() == instructorId) {
                    filteredSupportCalls.add(instructorSupportCall);
                    break;
                }
            }
        }

        return filteredSupportCalls;
    }

    private InstructorSupportCall create (InstructorSupportCall instructorSupportCall) {
        // Create StartDate
        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date utilDate = cal.getTime();
        java.sql.Date sqlDate = new Date(utilDate.getTime());

        instructorSupportCall.setStartDate(sqlDate);
        instructorSupportCall.setInstructorSupportCallResponses(null);

        return instructorSupportCallRepository.save(instructorSupportCall);
    }
}