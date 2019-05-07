package edu.ucdavis.dss.ipa.repositories;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import edu.ucdavis.dss.ipa.entities.EventLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class EventLogRepository {
    @Autowired
    private DynamoDBMapper mapper;

    public void save(EventLog eventLog) {
        mapper.save(eventLog);
    }

    public List<EventLog> findAll() {
        return mapper.scan(EventLog.class, new DynamoDBScanExpression());
    }
}