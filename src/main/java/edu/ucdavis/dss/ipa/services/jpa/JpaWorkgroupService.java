package edu.ucdavis.dss.ipa.services.jpa;

import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.WorkgroupRepository;
import edu.ucdavis.dss.ipa.services.WorkgroupService;

@Service
public class JpaWorkgroupService implements WorkgroupService {

	@Inject NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Inject WorkgroupRepository workgroupRepository;

	@Override
	@Transactional
	public Workgroup save(Workgroup workgroup)
	{
		return this.workgroupRepository.save(workgroup);
	}

	@Override
	public Workgroup findOneByCode(String code) {

		return this.workgroupRepository.findOneByCode(code);
	}

	@Override
	public List<Tag> getActiveTags(Workgroup workgroup) {
		return workgroup.getTags().stream().filter(t -> !t.isArchived()).collect(Collectors.toList());
	}

	/**
	 * Calculate the last time data in a workgroup was edited by an academic coordinator
	 * @param workgroup
	 * @return
	 */
	public String getLastActive (Workgroup workgroup) {
		String lastActive = null;

		// Collect scheduleIds
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("workgroupId", workgroup.getId());

		List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList(
				"select greatest(ifnull(max(cUpdatedAt), 0), ifnull(max(sgUpdatedAt), 0), ifnull(max(stUpdatedAt), 0), ifnull(max(aUpdatedAt), 0)) as lastActive from (" +
				" SELECT " +
				" max(c.UpdatedAt) as cUpdatedAt," +
				" max(sg.UpdatedAt) as sgUpdatedAt," +
				" max(st.UpdatedAt) as stUpdatedAt," +
				" max(a.UpdatedAt) as aUpdatedAt" +
				" FROM" +
				" Courses c" +
				" LEFT JOIN" +
				" Schedules s" +
				" ON c.ScheduleId = s.Id and c.ModifiedBy <> 'system'" +
				" LEFT JOIN" +
				" SectionGroups sg" +
				" ON sg.CourseId = c.Id  and sg.ModifiedBy <> 'system'" +
				" LEFT JOIN" +
				" Sections st" +
				" ON st.SectionGroupId = sg.Id  and st.ModifiedBy <> 'system'" +
				" LEFT JOIN" +
				" Activities a" +
				" ON a.SectionId = st.Id  and a.ModifiedBy <> 'system'" +
				" WHERE" +
				" s.WorkgroupId = :workgroupId" +
				" GROUP BY" +
				" c.Id," +
				" s.Id," +
				" sg.Id," +
				" st.Id," +
				" a.Id) fours", parameters);



		for(Map<String,Object> result : results) {
			lastActive = (String) result.get("lastActive");
			if ("0".equals(lastActive)) {
				lastActive = null;
			}
		}

		return lastActive;
	}

	/**
	 * Returns the latest date of the two. Handles null values, and will return null if both dates are null.
	 * @param newDate
	 * @param lastActive
     * @return
     */
	private Date calculateLastActive(Date newDate, Date lastActive) {
		if (lastActive == null) {
			return newDate;
		}

		// lastActive should be set to sectionGroupUpdatedAt if its a later date.
		if (newDate != null && newDate.compareTo(lastActive) > 0) {
			return newDate;
		}

		return lastActive;
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
	public List<Workgroup> findAll() {
		return (List<Workgroup>) this.workgroupRepository.findAll();
	}

	@Override
	public List<Long> findAllIds() {
		return (List<Long>) this.workgroupRepository.findAllIds();
	}

	@Override
	public void delete(Long workgroupId) {
		workgroupRepository.deleteById(workgroupId);
	}
}
