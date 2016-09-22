package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.ActivityLog;
import org.springframework.data.repository.CrudRepository;


public interface ActivityLogRepository extends CrudRepository<ActivityLog, Long> {

    ActivityLog findByActivityLogId(long activityLogId);

}


