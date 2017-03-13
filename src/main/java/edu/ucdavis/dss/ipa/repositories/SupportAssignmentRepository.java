package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SupportAssignment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface SupportAssignmentRepository extends CrudRepository<SupportAssignment, Long> {

    @Modifying
    @Transactional
    @Query(value="delete from SupportAssignment isa WHERE isa.id = ?1")
    void deleteById(long supportAssignmentId);

    SupportAssignment findById(Long id);
}
