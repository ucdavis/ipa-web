package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.security.Timestamp;
import java.util.List;


public interface ActivityLogRepository extends CrudRepository<ActivityLog, Long> {

    ActivityLog findById(long id);

    List<ActivityLog> findByTimestamp(long timestamp);

    @Query("SELECT activityLog FROM ActivityLog activityLog WHERE timestamp > :ts")
    List<ActivityLog> findAfterTimestamp(@Param("ts") long ts);

    @Query("SELECT activityLog FROM ActivityLog activityLog WHERE timestamp < :ts")
    List<ActivityLog> findBeforeTimestamp(@Param("ts") long ts);

    List<ActivityLog> findByMessage(String message);

    List<ActivityLog> findByUserId(long userId);

}


