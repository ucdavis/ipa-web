package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.Valid;
import java.util.List;

@Validated
public interface AuditLogService {
    AuditLog save(@Valid AuditLog auditLog);

    List<AuditLog> findByWorkgroupId(long workgroupId);

    List<AuditLog> findByWorkgroupIdAndModuleOrderByCreatedAtDesc(long workgroupId, String module);

    List<AuditLog> findByWorkgroupIdAndYearAndModuleOrderByCreatedAtDesc(long workgroupId, long year, String module);
}
