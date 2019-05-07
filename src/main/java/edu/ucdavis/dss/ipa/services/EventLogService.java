package edu.ucdavis.dss.ipa.services;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import edu.ucdavis.dss.ipa.entities.EventLog;
import edu.ucdavis.dss.ipa.repositories.EventLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventLogService {
    @Autowired
    DynamoDBMapper mapper;

    @Autowired
    private EventLogRepository eventLogRepository;

    public List<EventLog> getAllEventLogs() {
        List<EventLog> eventLogs = new ArrayList<>();

        eventLogRepository.findAll().forEach(eventLogs::add);

        return eventLogs;
    }

    public List<EventLog> getEventLogsByLogEntityId(String logEntityId) {
        Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
        eav.put(":val1", new AttributeValue().withS(logEntityId));

        DynamoDBQueryExpression<EventLog> queryExpression = new DynamoDBQueryExpression<EventLog>()
                .withKeyConditionExpression("logEntityId = :val1").withExpressionAttributeValues(eav);

        List<EventLog> eventLogs = mapper.query(EventLog.class, queryExpression);

        return eventLogs;
    }

    public void addEventLog(EventLog eventLog) {
        eventLogRepository.save(eventLog);
    }
}
