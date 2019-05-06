package edu.ucdavis.dss.ipa.services;

import edu.ucdavis.dss.ipa.entities.EventLog;
import edu.ucdavis.dss.ipa.repositories.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

@Service
public class EventLogService {
    @Autowired
    DynamoDBMapper mapper;

    @Autowired
    private EventLogRepository eventLogRepository;

    public List<EventLog> getAllEventLogs() {
        List<EventLog> events = new ArrayList<>();

        eventLogRepository.findAll().forEach(events::add);

        return events;
    }

    public List<EventLog> getEventLogsByLogEntityId(String logEntityId) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(logEntityId));

        DynamoDBQueryExpression<EventLog> queryExpression = new DynamoDBQueryExpression<EventLog>()
                .withKeyConditionExpression("logEntityId = :val1").withExpressionAttributeValues(eav);

        List<EventLog> events = mapper.query(EventLog.class, queryExpression);

        return events;
    }

    public void addEventLog(EventLog event) {
        eventLogRepository.save(event);
    }
}
