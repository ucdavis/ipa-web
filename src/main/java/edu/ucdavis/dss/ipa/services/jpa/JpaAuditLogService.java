package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.entities.SectionGroup;
import edu.ucdavis.dss.ipa.repositories.AuditLogRepository;
import edu.ucdavis.dss.ipa.services.AuditLogService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.List;

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
}
