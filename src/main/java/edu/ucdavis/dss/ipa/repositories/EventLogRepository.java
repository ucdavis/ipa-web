package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.EventLog;
import edu.ucdavis.dss.ipa.entities.EventLogId;

import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface EventLogRepository extends CrudRepository<EventLog, EventLogId> {
    List<EventLog> findByLogEntityId(String logEntityId);
}
