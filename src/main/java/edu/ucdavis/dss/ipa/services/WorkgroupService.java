package edu.ucdavis.dss.ipa.services;

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

	void delete(Long workgroupId);

	Workgroup findOneByCode(String string);

	List<Tag> getActiveTags(Workgroup workgroup);

	/**
	 * Returns true if workgroup has at least one UserRole for the specified loginId.
	 * @param workgroupId
	 * @param loginId
     * @return
     */
	boolean hasUser(Long workgroupId, String loginId);
}
