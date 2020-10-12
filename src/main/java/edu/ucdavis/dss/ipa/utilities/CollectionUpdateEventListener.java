package edu.ucdavis.dss.ipa.utilities;

import edu.ucdavis.dss.ipa.entities.AuditLog;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.event.spi.PostCollectionUpdateEvent;
import org.hibernate.event.spi.PostCollectionUpdateEventListener;
import org.springframework.data.annotation.Persistent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;

@Component
public class CollectionUpdateEventListener implements PostCollectionUpdateEventListener {

    @Inject
    Authorizer authorizer;

    @Inject
    WorkgroupService workgroupService;

    @Override
    public void onPostUpdateCollection(PostCollectionUpdateEvent postCollectionUpdateEvent){
        long start = System.currentTimeMillis();
        try {
            // Web request

            System.err.println("**********Stating Collection Update Listener*************");
            if (RequestContextHolder.getRequestAttributes() != null) {
                HandlerMethod handler = (HandlerMethod) RequestContextHolder.currentRequestAttributes()
                        .getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingHandler",
                                RequestAttributes.SCOPE_REQUEST);
                String moduleRaw = handler.getBean().toString();
                Object parentObj = postCollectionUpdateEvent.getAffectedOwnerOrNull();
                System.err.println("Collection update on " + parentObj.toString());
                PersistentCollection collection = postCollectionUpdateEvent.getCollection();
                try{
                    HashMap snapshot = (HashMap) collection.getStoredSnapshot();
                    Set<Map.Entry> set = snapshot.entrySet();
                    Iterator it = set.iterator();
                    while(it.hasNext()){
                        Object obj = it.next();
                        System.err.println("Relationship being update is " + obj.toString());
                    }
                } catch (Exception exception){
                    System.err.println("Error 1");
                }

                try{
                    List<Object> objects = (ArrayList) collection.getStoredSnapshot();
                    for(Object obj: objects){
                        System.err.println("Relationship being update is " + obj.toString());
                    }
                } catch (Exception exception){
                    System.err.println("Error 2");
                }


                /*if(ActivityLogFormatter.isAudited(moduleRaw, entityName)){

                }*/


                /*String entityName = entity.getClass().getSimpleName();
                if(ActivityLogFormatter.isAudited(moduleRaw, entityName)){
                    String module = ActivityLogFormatter.getFormattedModule(moduleRaw);
                    String entityDescription = ActivityLogFormatter.getFormattedEntityDescription(entity);
                    String userDisplayName = authorizer.getUserDisplayName();

                    UUID transactionId = UUID.randomUUID();
                    StringBuilder sb = new StringBuilder();
                    String endYear = ActivityLogFormatter.getYear(entity);
                    String startYear = String.valueOf(Integer.parseInt(endYear)-1);
                    String years = startYear + "-" + endYear;
                    sb.append("**" + userDisplayName + "**");
                    sb.append(" in **" + module + "** - **" + years + "**");
                    String termCode = ActivityLogFormatter.getTermCode(entity);
                    if(termCode.length() > 0){
                        sb.append(", **" + termCode + "**");
                    }

                    sb.append("\nInserted ");
                    sb.append(entityDescription);
                    System.err.println(sb.toString());

                    Session session = postInsertEvent.getPersister().getFactory().openTemporarySession();
                    AuditLog auditLogEntry = new AuditLog();
                    auditLogEntry.setMessage(sb.toString());
                    auditLogEntry.setLoginId(authorizer.getLoginId());
                    auditLogEntry.setUserName(userDisplayName);
                    auditLogEntry.setWorkgroup(workgroupService.findOneById(ActivityLogFormatter.getWorkgroupId(entity)));
                    auditLogEntry.setYear(Integer.parseInt(endYear));
                    auditLogEntry.setModule(module);
                    auditLogEntry.setTransactionId(transactionId);
                    session.save(auditLogEntry);
                    session.close();
                    System.err.println("*********Inserted to Audit Log + " + auditLogEntry.getId() + "************");
                } else {
                    System.err.println("Skipping insert of entity " + entityName + " from " + moduleRaw);
                }*/
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            //TODO explore options
            //ConsoleEmailService.reportException(ex, "Failed to log CRUD operations in activity log");
        }
        System.err.println("*********Ending Collection Update Listener took + " + (System.currentTimeMillis() - start) + "*************");
    }
}
