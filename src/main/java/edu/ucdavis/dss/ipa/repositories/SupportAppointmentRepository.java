package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SupportAppointment;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SupportAppointmentRepository extends CrudRepository<SupportAppointment, Long> {

    List<SupportAppointment> findByScheduleIdAndTermCode(long scheduleId, String termCode);
}
