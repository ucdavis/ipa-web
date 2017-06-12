package edu.ucdavis.dss.ipa.services;

import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.validation.annotation.Validated;

@Validated
public interface WorkgroupService {

	Workgroup save(@NotNull @Valid Workgroup workgroup);

	Workgroup findOneById(Long id);

	List<Schedule> getWorkgroupSchedulesForTenYears(
			Workgroup workgroup);

	List<Workgroup> findAll();

	List<Long> findAllIds();

	void delete(Long workgroupId);

	Workgroup findOneByCode(String string);

	List<Tag> getActiveTags(Workgroup workgroup);

	String getLastActive(Workgroup workgroup);
}
