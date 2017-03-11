package edu.ucdavis.dss.ipa.repositories;


import edu.ucdavis.dss.ipa.entities.InstructorSupportPreference;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InstructorInstructionalSupportPreferenceRepository extends CrudRepository<InstructorSupportPreference, Long> {

    @Modifying
    @Transactional
    @Query(value="delete from InstructorInstructionalSupportPreference sisp WHERE sisp.id = ?1")
    void deleteById(long studentInstructionalSupportPreferenceId);

    List<InstructorSupportPreference> findByInstructorIdAndInstructorInstructionalSupportCallId(long instructorId, long instructorSupportCallId);
}
