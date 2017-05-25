package edu.ucdavis.dss.ipa.services.jpa;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.*;
import org.springframework.jdbc.core.JdbcTemplate;
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
	@Override
	public Date getLastActive(Workgroup workgroup) {

		Date lastActive = null;

		// Gather entityIds for query
		List<Integer> scheduleIds = new ArrayList<>();
		List<Integer> courseIds = new ArrayList<>();
		List<Integer> sectionGroupIds = new ArrayList<>();
		List<Integer> sectionIds = new ArrayList<>();

		// Collect scheduleIds
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("workgroupId", workgroup.getId());

		List<Map<String, Object>> results = namedParameterJdbcTemplate.queryForList("SELECT * FROM Schedules WHERE WorkgroupId = :workgroupId", parameters);

		for(Map<String,Object> result : results) {
			int val = (int) result.get("id");
			scheduleIds.add(val);
		}

		if (scheduleIds.size() > 0) {
			// Collect courseIds
			parameters = new MapSqlParameterSource();
			parameters.addValue("scheduleIds", scheduleIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT * FROM Courses WHERE ScheduleId IN (:scheduleIds)", parameters);

			for(Map<String,Object> result : results) {
				Long val = (Long) result.get("id");
				courseIds.add(val.intValue());
			}
		}

		if (courseIds.size() > 0) {
			// Collect sectionGroupIds
			parameters = new MapSqlParameterSource();
			parameters.addValue("courseIds", courseIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT * FROM SectionGroups WHERE CourseId IN (:courseIds)", parameters);

			for(Map<String,Object> result : results) {
				int val = (int) result.get("id");
				sectionGroupIds.add(val);
			}
		}

		if (sectionGroupIds.size() > 0) {
			// Collect sectionIds
			parameters = new MapSqlParameterSource();
			parameters.addValue("sectionGroupIds", sectionGroupIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT * FROM Sections WHERE SectionGroupId IN (:sectionGroupIds)", parameters);

			for(Map<String,Object> result : results) {
				int val = (int) result.get("id");
				sectionIds.add(val);
			}
		}

		// Calculate courseUpdatedAt
		if (scheduleIds.size() > 0) {
			Date courseUpdatedAt = null;
			parameters = new MapSqlParameterSource();
			parameters.addValue("system", "system");
			parameters.addValue("scheduleIds", scheduleIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT max(updatedAt) as updatedAt FROM Courses WHERE ScheduleId IN (:scheduleIds) AND ModifiedBy != :system AND ModifiedBy IS NOT NULL", parameters);

			for(Map<String,Object> result : results) {
				courseUpdatedAt = (Date) result.get("updatedAt");
			}

			lastActive = courseUpdatedAt;
		}

		// Calculate sectionGroupUpdatedAt
		if (courseIds.size() > 0) {
			Date sectionGroupUpdatedAt = null;

			parameters = new MapSqlParameterSource();
			parameters.addValue("system", "system");
			parameters.addValue("courseIds", courseIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT max(updatedAt) as updatedAt FROM SectionGroups WHERE CourseId IN (:courseIds) AND ModifiedBy != :system AND ModifiedBy IS NOT NULL", parameters);

			for(Map<String,Object> result : results) {
				sectionGroupUpdatedAt = (Date) result.get("updatedAt");
			}

			lastActive = calculateLastActive(sectionGroupUpdatedAt, lastActive);
		}

		// Calculate sectionUpdatedAt
		if (sectionGroupIds.size() > 0) {
			Date sectionUpdatedAt = null;

			parameters = new MapSqlParameterSource();
			parameters.addValue("system", "system");
			parameters.addValue("sectionGroupIds", sectionGroupIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT max(updatedAt) as updatedAt FROM Sections WHERE SectionGroupId IN (:sectionGroupIds) AND ModifiedBy != :system AND ModifiedBy IS NOT NULL", parameters);

			for(Map<String,Object> result : results) {
				sectionUpdatedAt = (Date) result.get("updatedAt");
			}

			lastActive = calculateLastActive(sectionUpdatedAt, lastActive);
		}

		// Calculate activityUpdatedAt
		if (sectionIds.size() > 0) {
			Date activityUpdatedAt = null;

			parameters = new MapSqlParameterSource();
			parameters.addValue("system", "system");
			parameters.addValue("sectionIds", sectionIds);

			results = namedParameterJdbcTemplate.queryForList("SELECT max(updatedAt) as updatedAt FROM Activities WHERE SectionId IN (:sectionIds) AND ModifiedBy != :system AND ModifiedBy IS NOT NULL", parameters);

			for(Map<String,Object> result : results) {
				activityUpdatedAt = (Date) result.get("updatedAt");
			}

			lastActive = calculateLastActive(activityUpdatedAt, lastActive);
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
		workgroupRepository.delete(workgroupId);
	}
}
