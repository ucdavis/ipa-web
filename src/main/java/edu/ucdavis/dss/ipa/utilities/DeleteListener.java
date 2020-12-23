package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.hibernate.Session;
import org.hibernate.event.spi.PostCommitDeleteEventListener;
import org.hibernate.event.spi.PostDeleteEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import javax.inject.Inject;
import java.util.UUID;

@Component
public class DeleteListener implements PostCommitDeleteEventListener {
    @Inject
    Authorizer authorizer;

    @Inject
    WorkgroupService workgroupService;

    @Inject
    EmailService emailService;

    public void onPostDelete(PostDeleteEvent postDeleteEvent) {
        try {
            // Web request
            if (RequestContextHolder.getRequestAttributes() != null) {
                HandlerMethod handler = (HandlerMethod) RequestContextHolder.currentRequestAttributes()
                        .getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler",
                                RequestAttributes.SCOPE_REQUEST);

                String uri = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI();
                String endpoint = ActivityLogFormatter.getEndpoint(uri);
                String moduleRaw = handler.getBean().toString();
                Object entity = postDeleteEvent.getEntity();
                String entityName = entity.getClass().getSimpleName();

                if(ActivityLogFormatter.isAudited(moduleRaw, entityName, endpoint)){
                    String module = ActivityLogFormatter.getModuleDisplayName(moduleRaw, entity);
                    String entityDescription = ActivityLogFormatter.getFormattedEntityDescription(entity);
                    String userDisplayName = authorizer.getUserDisplayName();

                    UUID transactionId = UUID.randomUUID();
                    StringBuilder sb = new StringBuilder();
                    String year = ActivityLogFormatter.getYear(entity);
                    String years = ActivityLogFormatter.getYears(entity);
                    sb.append("**" + userDisplayName + "**");
                    sb.append(" in **" + module + "** - **" + years + "**");
                    String termCode = ActivityLogFormatter.getTermCode(entity);
                    if(termCode.length() > 0){
                        sb.append(", **" + termCode + "**");
                    }

                    sb.append("\nDeleted ");
                    sb.append("**" + entityDescription + "**");

                    Session session = postDeleteEvent.getPersister().getFactory().openTemporarySession();
                    AuditLog auditLogEntry = new AuditLog();
                    auditLogEntry.setMessage(sb.toString());
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
            emailService.reportException(ex, "Failed to log delete operation to audit log");
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return true;
    }

    @Override
    public void onPostDeleteCommitFailed(PostDeleteEvent postDeleteEvent){}
}
