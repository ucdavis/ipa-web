package edu.ucdavis.dss.ipa.repositories;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.ucdavis.dss.ipa.entities.Term;

public interface TermRepository extends CrudRepository<Term, Long> {
	Term findOneByTermCode(String termCode);

	@Query("SELECT t FROM Term t WHERE t.termCode LIKE :year%")
	List<Term> findByYear(@Param("year") String year);

	@Query("SELECT t FROM Term t WHERE t.termCode IN :termCodes AND (t.endDate > current_date() OR t.endDate IS NULL)")
	List<Term> findByTermCodeInAndExistingEndDateAfterNow(@Param("termCodes") Set<String> termCodes);

	List<Term> findByStartDateAfter(Date targetDate);

	/**
	 * Sorry
	 * @param loginId
	 * @return
	 */
	@Query(value = " SELECT DISTINCT(t.TermCode), t.BannerStartWindow1, t.BannerEndWindow1," +
			" t.BannerStartWindow2, t.BannerEndWindow2, t.StartDate, t.EndDate" +
			" FROM Users u, UserRoles ur, Schedules sch, Courses c, SectionGroups sg, Terms t" +
			" WHERE u.LoginId = :loginId " +
			" AND ur.UserId = u.Id " +
			" AND sch.WorkgroupId = ur.WorkgroupId " +
			" AND c.ScheduleId = sch.Id " +
			" AND sg.CourseId = c.Id " +
			" AND sg.TermCode = t.TermCode ", nativeQuery = true)
	List<Term> findByLoginId(@Param("loginId") String loginId);
}
