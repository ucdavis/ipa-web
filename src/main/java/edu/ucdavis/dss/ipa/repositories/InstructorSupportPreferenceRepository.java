package edu.ucdavis.dss.ipa.repositories;


import edu.ucdavis.dss.ipa.entities.InstructorSupportPreference;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface InstructorSupportPreferenceRepository extends CrudRepository<InstructorSupportPreference, Long> {

    @Modifying
    @Transactional
    @Query(value="delete from InstructorSupportPreference sisp WHERE sisp.id = ?1")
    void deleteById(long studentSupportPreferenceId);

    List<InstructorSupportPreference> findByInstructorId(long instructorId);
}
