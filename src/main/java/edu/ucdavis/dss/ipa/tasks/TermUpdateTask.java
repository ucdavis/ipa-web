package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.entities.Term;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.TermService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class TermUpdateTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

    @Inject
    DataWarehouseRepository dataWarehouseRepository;

    @Inject TermService termService;

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

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        List<DwTerm> dwTerms = dataWarehouseRepository.getTerms();

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
            try {
                if(dwTerm.getBeginDate() != null) {
                    localTerm.setStartDate(df.parse(dwTerm.getBeginDate()));
                }
                if(dwTerm.getEndDate() != null) {
                    localTerm.setEndDate(df.parse(dwTerm.getEndDate()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            this.termService.save(localTerm);
        }

        runningTask = false;
    }

}
