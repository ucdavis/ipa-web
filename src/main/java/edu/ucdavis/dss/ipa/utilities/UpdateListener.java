package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.security.Authorizer;
import javax.inject.Inject;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.hibernate.Session;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.HandlerMethod;

import java.util.UUID;


@Component
public class UpdateListener implements PostCommitUpdateEventListener {

    @Inject
    Authorizer authorizer;

    @Inject
    WorkgroupService workgroupService;

    @Inject
    EmailService emailService;

    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        try {
            // Web request
            if (RequestContextHolder.getRequestAttributes() != null) {
                HandlerMethod handler = (HandlerMethod) RequestContextHolder.currentRequestAttributes()
                        .getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler",
                                RequestAttributes.SCOPE_REQUEST);

                String uri = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getRequestURI();
                String endpoint = ActivityLogFormatter.getEndpoint(uri);
                String moduleRaw = handler.getBean().toString();
                Object entity = postUpdateEvent.getEntity();
                String entityName = entity.getClass().getSimpleName();

                if (ActivityLogFormatter.isAudited(moduleRaw, entityName, endpoint)) {
                    String module = ActivityLogFormatter.getModuleDisplayName(moduleRaw, entity);

                    String[] props =
                            postUpdateEvent.getPersister().getEntityMetamodel().getPropertyNames();
                    Object[] oldState = postUpdateEvent.getOldState();
                    Object[] state = postUpdateEvent.getState();
                    String userDisplayName = authorizer.getUserDisplayName();

                    UUID transactionId = UUID.randomUUID();
                    for (int i : postUpdateEvent.getDirtyProperties()) {
                        if (!ActivityLogFormatter.isFieldAudited(moduleRaw, entityName, props[i])) {
                            continue;
                        }
                        String message = ActivityLogFormatter.getFormattedUpdateAction(
                                module,
                                entity,
                                props[i],
                                oldState[i],
                                state[i],
                                userDisplayName);
                        String year = ActivityLogFormatter.getYear(entity);

                        Session session = postUpdateEvent.getPersister().getFactory().openTemporarySession();
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
            }
        } catch (Exception ex) {
            emailService.reportException(ex, "Failed to log update operation(s) to audit log");
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return true;
    }

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent postUpdateEvent){}
}