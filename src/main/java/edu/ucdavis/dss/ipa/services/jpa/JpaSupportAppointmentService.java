package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import edu.ucdavis.dss.ipa.repositories.SupportAppointmentRepository;
import edu.ucdavis.dss.ipa.services.SupportAppointmentService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class JpaSupportAppointmentService implements SupportAppointmentService {
    @Inject SupportAppointmentRepository supportAppointmentRepository;

    @Override
    public void delete(Long supportAppointmentId) {
        this.supportAppointmentRepository.delete(supportAppointmentId);
    }

    @Override
    public SupportAppointment create(SupportAppointment supportAppointment) {
        return this.supportAppointmentRepository.save(supportAppointment);
    }

    @Override
    public List<SupportAppointment> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        return this.supportAppointmentRepository.findByScheduleIdAndTermCode(scheduleId, termCode);
    }
}
