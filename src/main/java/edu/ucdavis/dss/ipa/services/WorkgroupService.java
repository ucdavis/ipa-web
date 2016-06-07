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
	Workgroup saveWorkgroup(@NotNull @Valid Workgroup workgroup);

	Workgroup findOneById(Long id);

	List<Schedule> getWorkgroupSchedulesForTenYears(
			Workgroup workgroup);

	List<Workgroup> getAllWorkgroups();

	/**
	 * Overloaded version of {@link #getWorkgroupTeachingCallResponsesByInstructorId(Workgroup, Instructor, int)}.
	 * <p>
	 * Implementation defaults to returning a List of TeachingCallResponses for ten years for a 
	 * given Workgroup/Instructor combination.
	 * 
	 * @param workgroup Workgroup from which to get TeachingCallResponses
	 * @param instructor Instructor whose TeachingCallResponses should be retrieved
	 * @return List of TeachingCallResponses for (10) years
	 */
	List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor);
	
	List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor, int years);

	void deleteByWorkgroupId(Long workgroupId);

	Workgroup findOneByCode(String string);
}
