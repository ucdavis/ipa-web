package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportStaff;

public interface InstructionalSupportStaffService {
    InstructionalSupportStaff findOneById(long instructionalSupportStaffId);

    InstructionalSupportStaff findOrCreate(String firstName, String lastName, String email, String loginId);
}
