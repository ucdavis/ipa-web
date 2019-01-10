package edu.ucdavis.dss.ipa.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ActivityLog {
    private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

    private List<ActivityLogTag> activityLogTags = new ArrayList<>();

    private long id;
    private Timestamp timestamp;
    private String message;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", unique = true, nullable = false)
    @JsonProperty
    public long getId() { return this.id; }

    public void setId(long id) { this.id = id; }

    @Basic
    @Column(name = "Timestamp", unique = false, nullable = false)
    @JsonProperty
    public Timestamp getTimestamp() { return this.timestamp; }

    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    @Basic
    @Column(name = "Message", unique = false, nullable = false)
    @JsonProperty
    public String getMessage() { return this.message; }

    public void setMessage(String message) { this.message = message; }

    @OneToMany (fetch = FetchType.EAGER, mappedBy = "activityLog")
    @JsonIgnore
    public List<ActivityLogTag> getActivityLogTags() {
        return this.activityLogTags;
    }

    public void setActivityLogTags(List<ActivityLogTag> activityLogTags) { this.activityLogTags = activityLogTags; }

    /**
     * Adds a new tag to the activity log
     * @param referenceEntity - The entity to tag on the activity log entry
     */
    public void addTag(Object referenceEntity) {
        ActivityLogTag newTag = new ActivityLogTag();
        newTag.setActivityLog(this);

        // Initialize tag with the name of the entity
        String loggableTag = referenceEntity.getClass().getSimpleName().toLowerCase();

        try {
            Method getEntityId = referenceEntity.getClass().getMethod("getId", (java.lang.Class<?>[])null);
            long entityId = (long) getEntityId.invoke(referenceEntity, (java.lang.Object[])null);

            loggableTag += "_" + entityId;
        } catch (InvocationTargetException e) {
            log.debug("Invocation Target Exception when trying to get method getId on " + loggableTag);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            log.debug("Illegal Access Exception when trying to get method getId on " + loggableTag);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            log.debug("Could not find method getId for " + loggableTag);
            e.printStackTrace();
        }

        newTag.setTag(loggableTag);
        this.activityLogTags.add(newTag);
    }
}
