package edu.ucdavis.dss.ipa.services.jpa;


import edu.ucdavis.dss.ipa.entities.InstructorInstructionalSupportCall;
import edu.ucdavis.dss.ipa.repositories.InstructorInstructionalSupportCallRepository;
import edu.ucdavis.dss.ipa.services.InstructorInstructionalSupportCallService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
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

    private InstructorInstructionalSupportCall create (InstructorInstructionalSupportCall instructorInstructionalSupportCall) {
        return instructorInstructionalSupportCallRepository.save(instructorInstructionalSupportCall);
    }
}