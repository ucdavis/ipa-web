package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.SyncAction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import javax.transaction.Transactional;

public interface SyncActionRepository extends CrudRepository<SyncAction, Long> {
    @Modifying
    @Transactional
    @Query(value="delete from SyncAction sa WHERE sa.id = ?1")
    void deleteById(long syncActionId);
}
