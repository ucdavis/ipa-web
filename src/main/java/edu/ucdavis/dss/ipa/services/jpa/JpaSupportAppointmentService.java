package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import edu.ucdavis.dss.ipa.repositories.SupportAppointmentRepository;
import edu.ucdavis.dss.ipa.services.SupportAppointmentService;
import org.springframework.stereotype.Service;

import jakarta.inject.Inject;
import java.util.List;

@Service
public class JpaSupportAppointmentService implements SupportAppointmentService {
    @Inject SupportAppointmentRepository supportAppointmentRepository;

    @Override
    public void delete(Long supportAppointmentId) {
        this.supportAppointmentRepository.deleteById(supportAppointmentId);
    }

    @Override
    public SupportAppointment create(SupportAppointment supportAppointment) {
        return this.supportAppointmentRepository.save(supportAppointment);
    }

    @Override
    public List<SupportAppointment> findByScheduleIdAndTermCode(long scheduleId, String termCode) {
        return this.supportAppointmentRepository.findByScheduleIdAndTermCode(scheduleId, termCode);
    }

    @Override
    public SupportAppointment createOrUpdate(SupportAppointment supportAppointmentDTO) {
        SupportAppointment originalSupportAppointment = this.findByScheduleIdAndTermCodeAndSupportStaffId(
                supportAppointmentDTO.getSchedule().getId(),
                supportAppointmentDTO.getTermCode(),
                supportAppointmentDTO.getSupportStaff().getId());

        if (originalSupportAppointment == null) {
            originalSupportAppointment = new SupportAppointment();

            originalSupportAppointment.setSupportStaff(supportAppointmentDTO.getSupportStaff());
            originalSupportAppointment.setSchedule(supportAppointmentDTO.getSchedule());
            originalSupportAppointment.setTermCode(supportAppointmentDTO.getTermCode());
        }

        originalSupportAppointment.setPercentage(supportAppointmentDTO.getPercentage());
        originalSupportAppointment.setType(supportAppointmentDTO.getType());

        return this.supportAppointmentRepository.save(originalSupportAppointment);
    }

    private SupportAppointment findByScheduleIdAndTermCodeAndSupportStaffId(long scheduleId, String termCode, long supportStaffId) {
        return this.supportAppointmentRepository.findByScheduleIdAndTermCodeAndSupportStaffId(scheduleId, termCode, supportStaffId);
    }
}
