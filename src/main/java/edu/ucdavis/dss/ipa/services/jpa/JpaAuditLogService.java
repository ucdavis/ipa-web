package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.repositories.AuditLogRepository;
import edu.ucdavis.dss.ipa.services.AuditLogService;
import java.util.List;
import javax.inject.Inject;
import org.springframework.stereotype.Service;

@Service
public class JpaAuditLogService implements AuditLogService {
    @Inject
    AuditLogRepository auditLogRepository;

    @Override
    public AuditLog save(AuditLog auditLog) {
        return this.auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLog> findByWorkgroupId(long workgroupId){
        return this.auditLogRepository.findByWorkgroupId(workgroupId);
    }

    @Override
    public List<AuditLog> findByWorkgroupIdAndModule(long workgroupId, String module){
        return this.auditLogRepository.findByWorkgroupIdAndModule(workgroupId, module);
    }

    @Override
    public List<AuditLog> findByWorkgroupIdAndModuleOrderByCreatedAtDesc(long workgroupId,
                                                                         String module) {
        return this.auditLogRepository.findByWorkgroupIdAndModuleOrderByCreatedAtDesc(workgroupId, module);
    }

    @Override
    public List<AuditLog> findByWorkgroupIdAndYearAndModuleOrderByCreatedAtDesc(long workgroupId,
                                                                         long year,
                                                                         String module) {
        return this.auditLogRepository.findByWorkgroupIdAndYearAndModuleOrderByCreatedAtDesc(workgroupId, year, module);
    }
}
