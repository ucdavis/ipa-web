package edu.ucdavis.dss.ipa.entities;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;

import java.io.Serializable;

/**
 * Composite Key to support a HASH+RANGE key of DynamoDB in Spring-Data
 */

@DynamoDBDocument
public class EventLogId implements Serializable {
    private static final long serialVersioonUID = 1L;

    @DynamoDBRangeKey(attributeName = "id")
    private String id;

    @DynamoDBHashKey(attributeName = "logEntityId")
    private String logEntityId;

    public EventLogId() {
    }

    public EventLogId(String id, String logEntityId) {
        this.id = id;
        this.logEntityId = logEntityId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLogEntityId() {
        return logEntityId;
    }

    public void setLogEntityId(String logEntityId) {
        this.logEntityId = logEntityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EventLogId eventLogId = (EventLogId) o;
        if (!getLogEntityId().equals(eventLogId.getLogEntityId()))
            return false;
        return getId().equals(eventLogId.getId());
    }

    @Override
    public int hashCode() {
        int result = getLogEntityId().hashCode();
        result = 31 * result + getId().hashCode();
        return result;
    }
}
