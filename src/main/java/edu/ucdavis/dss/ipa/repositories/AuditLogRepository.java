package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuditLogRepository extends CrudRepository<AuditLog, Long>  {
    @Query( " SELECT DISTINCT al" +
            " FROM AuditLog al" +
            " WHERE al.workgroup.id = :workgroupId ")
    List<AuditLog> findByWorkgroupId(@Param("workgroupId") long workgroupId);

    @Query( " SELECT DISTINCT al" +
            " FROM AuditLog al" +
            " WHERE al.workgroup.id = :workgroupId " +
            " AND al.module = :module")
    List<AuditLog> findByWorkgroupIdAndModule(
            @Param("workgroupId") long workgroupId,
            @Param("module") String module);

    List<AuditLog> findByWorkgroupIdAndModuleOrderByCreatedAtDesc(long workgroupId, String module);
}
