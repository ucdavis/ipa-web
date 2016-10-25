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

	@Query("SELECT t FROM Term t WHERE t.termCode IN :termCodes AND (t.endDate > current_date() OR t.endDate IS NULL)")
	List<Term> findByTermCodeInAndExistingEndDateAfterNow(@Param("termCodes") Set<String> termCodes);

	List<Term> findByStartDateAfter(Date targetDate);

	/**
	 * Sorry
	 * @param loginId
	 * @return
	 */
	@Query(value = " SELECT DISTINCT t" +
			" FROM User u, UserRole ur, Schedule sch, Course c, SectionGroup sg, Term t" +
			" WHERE u.loginId = :loginId " +
			" AND ur.user = u " +
			" AND sch.workgroup = ur.workgroup " +
			" AND c.schedule = sch " +
			" AND sg.course = c " +
			" AND sg.termCode = t.termCode ")
	List<Term> findByLoginId(@Param("loginId") String loginId);

	List<Term> findByTermCodeIn(Set<String> termCodes);

	@Query("SELECT DISTINCT t " +
			" FROM SectionGroup sg, Course c, Schedule sch, Term t " +
			" WHERE sg.course = c " +
			" AND sg.termCode = t.termCode" +
			" AND c.schedule.id = sch.id" +
			" AND sch.workgroup.id = :workgroupId ")
	List<Term> findActiveTermCodesByWorkgroupId(@Param("workgroupId") long workgroupId);
}
