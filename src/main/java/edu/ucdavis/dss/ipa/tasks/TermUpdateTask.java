package edu.ucdavis.dss.ipa.tasks;

import java.util.List;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.TermService;

@Service
public class TermUpdateTask {
	private static final Logger log = LogManager.getLogger();

	@Inject TermService termService;
	@Inject DataWarehouseRepository dwRepository;

	private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

	@Scheduled( fixedDelay = 86400000 ) // Every 24 hours
	@Async
	public void updateTerms() {
		if(runningTask) return; // avoid multiple concurrent jobs

		runningTask = true;

		List<DwTerm> dwTerms = null;

		try {
			dwTerms = dwRepository.getAllTerms();
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}

		if(dwTerms != null) {
			for(DwTerm dwTerm : dwTerms) {
				Term term = this.termService.findOrCreateByTermCode(dwTerm.getCode());
				term.setStartDate(dwTerm.getBeginDate());
				term.setEndDate(dwTerm.getEndDate());
				this.termService.save(term);
			}
		}

		if (dwTerms != null) {
			log.info("Finished updating terms from DW. A total of " + dwTerms.size() + " terms.");
		}

		runningTask = false;
	}
}
