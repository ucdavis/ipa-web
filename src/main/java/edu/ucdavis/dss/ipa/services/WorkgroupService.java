package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface WorkgroupService {

	Workgroup save(@NotNull @Valid Workgroup workgroup);

	Workgroup findOneById(Long id);

	List<Schedule> getWorkgroupSchedulesForTenYears(
			Workgroup workgroup);

	List<Workgroup> findAll();

	void delete(Long workgroupId);

	Workgroup findOneByCode(String string);
}
