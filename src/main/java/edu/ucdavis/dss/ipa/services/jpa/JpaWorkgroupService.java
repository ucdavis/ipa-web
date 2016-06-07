package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.entities.TeachingCallResponse;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.TeachingCallResponseRepository;
import edu.ucdavis.dss.ipa.repositories.WorkgroupRepository;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaWorkgroupService implements WorkgroupService {
	@Inject WorkgroupRepository workgroupRepository;
	@Inject TeachingCallResponseRepository teachingCallResponseRepository;

	@Override
	@Transactional
	public Workgroup saveWorkgroup(Workgroup workgroup)
	{
		return this.workgroupRepository.save(workgroup);
	}

	@Override
	public Workgroup findOneByCode(String code) {
		return this.workgroupRepository.findOneByCode(code);
	}

	@Override
	public Workgroup findOneById(Long id) {
		return this.workgroupRepository.findOneById(id);
	}

	private Schedule getWorkgroupScheduleByYear(long y, Workgroup workgroup) {
		for(Schedule schedule : workgroup.getSchedules()) {
			if (schedule.getYear() == y) {
				return schedule;
			}
		}
		Schedule blankSchedule = new Schedule();
		blankSchedule.setYear(y);
		return blankSchedule;
	}

	@Override
	public List<Schedule> getWorkgroupSchedulesForTenYears(Workgroup workgroup) {
		long thisYear = Calendar.getInstance().get(Calendar.YEAR);
		List<Schedule> scheduleList = new ArrayList<Schedule>();
		if (workgroup != null) {
			for (long y = thisYear + 2; y > thisYear - 8; y--) {
				scheduleList.add(this.getWorkgroupScheduleByYear(y, workgroup));
			}
		}
		return scheduleList;
	}

	@Override
	public List<Workgroup> getAllWorkgroups() {
		return (List<Workgroup>) this.workgroupRepository.findAll();
	}

	@Override
	public List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor, int years) {
		if (workgroup == null || instructor == null)
			return null;
		
		long thisYear = Calendar.getInstance().get(Calendar.YEAR);
		List<Long> yearsList = LongStream.rangeClosed(thisYear - years + 1, thisYear).boxed().collect(Collectors.toList());
		List<TeachingCallResponse> teachingCallResponses = this.teachingCallResponseRepository.findByInstructorIdAndTeachingCallScheduleWorkgroupIdAndTeachingCallScheduleYearIn(instructor.getId(), workgroup.getId(), yearsList);
		
		return teachingCallResponses;
	}

	@Override
	public List<TeachingCallResponse> getWorkgroupTeachingCallResponsesByInstructorId(
			Workgroup workgroup, Instructor instructor) {
		int NUMBER_OF_YEARS = 10;
		return getWorkgroupTeachingCallResponsesByInstructorId(workgroup, instructor, NUMBER_OF_YEARS);
	}

	@Override
	public void deleteByWorkgroupId(Long workgroupId) {
		workgroupRepository.delete(workgroupId);
	}
}
