package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface SupportAppointmentService {
    void delete(Long supportAppointmentId);

    SupportAppointment create (SupportAppointment supportAppointment);

    List<SupportAppointment> findByScheduleIdAndTermCode(long id, String termCode);

    SupportAppointment createOrUpdate(SupportAppointment supportAppointment);
}
