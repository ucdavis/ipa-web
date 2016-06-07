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

}
