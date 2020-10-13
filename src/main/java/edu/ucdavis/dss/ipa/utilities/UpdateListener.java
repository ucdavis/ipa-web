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
import org.springframework.web.method.HandlerMethod;

import java.util.UUID;


@Component
public class UpdateListener implements PostCommitUpdateEventListener {

    @Inject
    Authorizer authorizer;

    @Inject
    WorkgroupService workgroupService;

    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        long start = System.currentTimeMillis();
        try {
            // Web request

            System.err.println("**********Stating Update Listener*************");
            if (RequestContextHolder.getRequestAttributes() != null) {
                HandlerMethod handler = (HandlerMethod) RequestContextHolder.currentRequestAttributes()
                        .getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler",
                                RequestAttributes.SCOPE_REQUEST);
                String moduleRaw = handler.getBean().toString();
                Object entity = postUpdateEvent.getEntity();
                String entityName = entity.getClass().getSimpleName();

                String module = ActivityLogFormatter.getFormattedModule(moduleRaw);
                String entityDescription = ActivityLogFormatter.getFormattedEntityDescription(entity);

                String[] props =
                        postUpdateEvent.getPersister().getEntityMetamodel().getPropertyNames();
                Object[] oldState = postUpdateEvent.getOldState();
                Object[] state = postUpdateEvent.getState();
                String userDisplayName = authorizer.getUserDisplayName();

                UUID transactionId = UUID.randomUUID();
                for (int i : postUpdateEvent.getDirtyProperties()) {
                    StringBuilder sb = new StringBuilder();
                    if (!ActivityLogFormatter.isAudited(moduleRaw, entityName, props[i])) {
                        System.err.println("Skipping prop " + props[i] + " on entity " + entityName + " from " + moduleRaw);
                        continue;
                    }
                    String year = ActivityLogFormatter.getYear(entity);
                    String years = ActivityLogFormatter.getYears(entity);
                    sb.append("**" + userDisplayName + "**");
                    sb.append(" in **" + module + "** - **" + years + "**");
                    String termCode = ActivityLogFormatter.getTermCode(entity);
                    if(termCode.length() > 0){
                        sb.append(", **" + termCode + "**");
                    }

                    sb.append("\nChanged ");
                    sb.append(entityDescription);
                    sb.append(" **" + ActivityLogFormatter.getFormattedPropName(props[i]) + "** from **" + ActivityLogFormatter.getEntityDisplayName(props[i], oldState[i]) + "** to **" + ActivityLogFormatter.getEntityDisplayName(props[i], state[i]) + "**");
                    System.err.println(sb.toString());

                    Session session = postUpdateEvent.getPersister().getFactory().openTemporarySession();
                    AuditLog auditLogEntry = new AuditLog();
                    auditLogEntry.setMessage(sb.toString());
                    auditLogEntry.setLoginId(authorizer.getLoginId());
                    auditLogEntry.setUserName(userDisplayName);
                    auditLogEntry.setWorkgroup(workgroupService.findOneById(ActivityLogFormatter.getWorkgroupId(entity)));
                    auditLogEntry.setYear(Integer.parseInt(year));
                    auditLogEntry.setModule(module);
                    auditLogEntry.setTransactionId(transactionId);
                    session.save(auditLogEntry);
                    session.close();
                    System.err.println("*********Inserted to Audit Log + " + auditLogEntry.getId() + "************");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //TODO explore options
            //ConsoleEmailService.reportException(ex, "Failed to log CRUD operations in activity log");
        }
        System.err.println("*********Ending Update Listener took + " + (System.currentTimeMillis() - start) + "ms*************");
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return true;
    }

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent postUpdateEvent){}
}