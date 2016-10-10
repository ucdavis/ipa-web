package edu.ucdavis.dss.ipa.services.jpa;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.repositories.TermRepository;
import edu.ucdavis.dss.ipa.services.TermService;

@Service
public class JpaTermService implements TermService {
	@Inject TermRepository termRepository;
	
	@Override
	@Transactional
	public Term save(Term term)
	{
		return this.termRepository.save(term);
	}

	@Override
	public Term getOneByTermCode(String termCode) {
		return this.termRepository.findOneByTermCode(termCode);
	}

	@Override
	public Term findOrCreateByTermCode(String termCode) {
		if (termCode == null) return null;

		Term term = this.termRepository.findOneByTermCode(termCode);

		if (term == null) {
			term = new Term();
			term.setTermCode(termCode);
			term = this.termRepository.save(term);
		}

		return term;
	}

	@Override
	public List<Term> findByYear(long year) {
		return this.termRepository.findByYear(year);
	}

	@Override
	public Term updateOrCreate(Term term) {
		Term updatedTerm = this.getOneByTermCode(term.getTermCode());
		if(updatedTerm == null) {
			updatedTerm = new Term();
		}
		updatedTerm.setTermCode(term.getTermCode());
		updatedTerm.setBannerEndWindow1(term.getBannerEndWindow1());
		updatedTerm.setBannerStartWindow1(term.getBannerStartWindow1());

		this.save(updatedTerm);

		return updatedTerm;
	}

	@Override
	public List<Term> findByTermCodeInAndExistingEndDateAfterNow(Set<String> termCodes) {
		return this.termRepository.findByTermCodeInAndExistingEndDateAfterNow(termCodes);
	}

	@Override
	public List<Term> findByStartDateAfter(Date targetDate) {
		return this.termRepository.findByStartDateAfter(targetDate);
	}

	@Override
	public List<Term> findByLoginId(String loginId) {
		return termRepository.findByLoginId(loginId);
	}

	@Override
	public Boolean isHistoricalByTermCode(String termCode) {
		Term term = this.getOneByTermCode(termCode);
		Date now = Calendar.getInstance().getTime();

		if (term == null) {
			return false;
		}

		// Returns negative if 'now' comes after 'endDate'
		// Zero if they are equal
		// Positive if 'now' comes before 'endDate'
		return now.compareTo(term.getEndDate()) > 0;
	}
}
