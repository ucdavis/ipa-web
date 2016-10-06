package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.repositories.RestDataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.TermService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;

@Service
public class TermUpdateTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

    @Inject
    RestDataWarehouseRepository restDataWarehouseRepository;

    @Inject
    TermService termService;

    /**
     * Queries Data Warehouse for term information and updates the local
     * 'Terms' table with term start/end dates. This is needed to lock
     * schedules, etc.
     */
    @Scheduled( fixedDelay = 43200000 ) // every 12 hours
    @Async
    public void updateTermsFromDW() {
        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        List<DwTerm> dwTerms = restDataWarehouseRepository.getTerms();

        // Loop through DW terms and update our local terms, creating them
        // if necessary.
        for(DwTerm dwTerm : dwTerms) {
            Term localTerm = this.termService.getOneByTermCode(dwTerm.getCode());

            if(localTerm == null) {
                localTerm = new Term();
                localTerm.setTermCode(dwTerm.getCode());
            }

            if(dwTerm.getMaintenanceDate1Start() != null) {
                localTerm.setBannerStartWindow1(new Date(Long.parseLong(dwTerm.getMaintenanceDate1Start())));
            }
            if(dwTerm.getMaintenanceDate1End() != null) {
                localTerm.setBannerEndWindow1(new Date(Long.parseLong(dwTerm.getMaintenanceDate1End())));
            }
            if(dwTerm.getMaintenanceDate2Start() != null) {
                localTerm.setBannerStartWindow2(new Date(Long.parseLong(dwTerm.getMaintenanceDate2Start())));
            }
            if(dwTerm.getMaintenanceDate2End() != null) {
                localTerm.setBannerEndWindow2(new Date(Long.parseLong(dwTerm.getMaintenanceDate2End())));
            }
            if(dwTerm.getBeginDate() != null) {
                localTerm.setStartDate(new Date(Long.parseLong(dwTerm.getBeginDate())));
            }
            if(dwTerm.getEndDate() != null) {
                localTerm.setEndDate(new Date(Long.parseLong(dwTerm.getEndDate())));
            }

            this.termService.save(localTerm);
        }

        runningTask = false;
    }

}
