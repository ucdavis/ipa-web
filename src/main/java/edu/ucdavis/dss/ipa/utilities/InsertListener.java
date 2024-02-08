package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.hibernate.Session;
import org.hibernate.event.spi.PostCommitInsertEventListener;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import jakarta.inject.Inject;
import java.util.UUID;

@Component
public class InsertListener implements PostCommitInsertEventListener
{
    @Inject
    Authorizer authorizer;

    @Inject
    WorkgroupService workgroupService;

    @Inject
    EmailService emailService;

    public void onPostInsert(PostInsertEvent postInsertEvent) {
        try {
            // Web request
            if (RequestContextHolder.getRequestAttributes() != null) {
                HandlerMethod handler = (HandlerMethod) RequestContextHolder.currentRequestAttributes()
                        .getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler",
                                RequestAttributes.SCOPE_REQUEST);

                String uri = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI();
                String endpoint = ActivityLogFormatter.getEndpoint(uri);
                String moduleRaw = handler.getBean().toString();
                Object entity = postInsertEvent.getEntity();
                String entityName = entity.getClass().getSimpleName();

                if(ActivityLogFormatter.isAudited(moduleRaw, entityName, endpoint)){
                    String module = ActivityLogFormatter.getModuleDisplayName(moduleRaw, entity);
                    String userDisplayName = authorizer.getUserDisplayName();
                    String year = ActivityLogFormatter.getYear(entity);

                    UUID transactionId = UUID.randomUUID();
                    String message = ActivityLogFormatter.getFormattedInsertAction(
                        module,
                        entity,
                        userDisplayName
                    );

                    Session session = postInsertEvent.getPersister().getFactory().openTemporarySession();
                    AuditLog auditLogEntry = new AuditLog();
                    auditLogEntry.setMessage(message);
                    auditLogEntry.setLoginId(authorizer.getLoginId());
                    auditLogEntry.setUserName(userDisplayName);
                    auditLogEntry.setWorkgroup(workgroupService.findOneById(ActivityLogFormatter.getWorkgroupId(entity)));
                    auditLogEntry.setYear(Integer.parseInt(year));
                    auditLogEntry.setModule(ActivityLogFormatter.getFormattedModule(moduleRaw, entity));
                    auditLogEntry.setTransactionId(transactionId);
                    session.save(auditLogEntry);
                    session.close();
                }
            }
        } catch (Exception ex) {
            emailService.reportException(ex, "Failed to log insert operation to audit log");
        }
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister entityPersister) {
        return true;
    }

    @Override
    public void onPostInsertCommitFailed(PostInsertEvent postInsertEvent){}
}
