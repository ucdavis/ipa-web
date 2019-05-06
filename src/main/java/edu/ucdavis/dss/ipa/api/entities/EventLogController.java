package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.EventLog;
import edu.ucdavis.dss.ipa.services.EventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
public class EventLogController {

    @Autowired
    private EventLogService eventLogService;

    @RequestMapping("status")
    public HashMap<String, String> status() {
        HashMap<String, String> status = new HashMap<>();

        status.put("status", "ok");

        return status;
    }

    @RequestMapping(value = "/eventLogs", method = RequestMethod.GET)
    public List<EventLog> getAllEventLogs() {
        return eventLogService.getAllEventLogs();
    }

    @RequestMapping(value = "/eventLogs/{logEntityId}", method = RequestMethod.GET)
    public List<EventLog> getEventLogsByLogEntityId(@PathVariable String logentityId) {
        return eventLogService.getEventLogsByLogEntityId(logentityId);
    }
}
