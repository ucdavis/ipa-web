package edu.ucdavis.dss.ipa.services.jpa;


import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;
import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCallResponse;
import edu.ucdavis.dss.ipa.repositories.InstructorInstructionalSupportCallRepository;
import edu.ucdavis.dss.ipa.services.InstructorInstructionalSupportCallService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaInstructorInstructionalSupportCallService implements InstructorInstructionalSupportCallService {

    @Inject InstructorInstructionalSupportCallRepository instructorInstructionalSupportCallRepository;

    @Override
    public InstructorInstructionalSupportCall findOneById(long instructorInstructionalSupportCallId) {
        return instructorInstructionalSupportCallRepository.findById(instructorInstructionalSupportCallId);
    }

    @Override
    public InstructorInstructionalSupportCall findOrCreate(InstructorInstructionalSupportCall instructorInstructionalSupportCall) {
        return this.create(instructorInstructionalSupportCall);
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
        return instructorInstructionalSupportCallRepository.save(instructorInstructionalSupportCall);
    }
}