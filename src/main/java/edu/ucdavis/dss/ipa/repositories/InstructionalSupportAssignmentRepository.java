package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.InstructionalSupportAssignment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface InstructionalSupportAssignmentRepository extends CrudRepository<InstructionalSupportAssignment, Long> {

    @Modifying
    @Transactional
    @Query(value="delete from InstructionalSupportAssignment isa WHERE isa.id = ?1")
    void deleteById(long instructionalSupportAssignmentId);

    InstructionalSupportAssignment findById(Long id);
}
