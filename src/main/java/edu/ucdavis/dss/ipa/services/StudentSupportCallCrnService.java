package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.StudentSupportCallCrn;

public interface StudentSupportCallCrnService {
    StudentSupportCallCrn create (StudentSupportCallCrn studentSupportCallCrn);

    void delete(Long studentSupportCallCrnId);
}
