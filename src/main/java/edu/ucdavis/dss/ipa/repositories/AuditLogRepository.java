package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends CrudRepository<AuditLog, Long>  {

    List<AuditLog> findByWorkgroupId(long workgroupId);

    List<AuditLog> findByWorkgroupIdAndModuleOrderByCreatedAtDesc(long workgroupId, String module);

    List<AuditLog> findByWorkgroupIdAndYearAndModuleOrderByCreatedAtDesc(long workgroupId, long year, String module);
}
