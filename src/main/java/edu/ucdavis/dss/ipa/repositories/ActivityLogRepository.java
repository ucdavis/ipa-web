package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import edu.ucdavis.dss.ipa.entities.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

//public interface ActivityLogRepository extends CrudRepository<ActivityLog, Long> {
//
//    ActivityLog findById(long id);
//
//    List<ActivityLog> findByTimestamp(Timestamp timestamp);
//
//    List<ActivityLog> findByTimestampGreaterThan(Timestamp timestamp);
//
//    List<ActivityLog> findByTimestampLessThan(Timestamp timestamp);
//
//    List<ActivityLog> findByMessage(String message);
//
//    List<ActivityLog> findByUser(User user);
//
//}
