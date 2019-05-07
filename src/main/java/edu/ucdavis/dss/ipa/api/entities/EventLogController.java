package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.EventLog;
import edu.ucdavis.dss.ipa.services.EventLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/eventLogs", method = RequestMethod.POST)
    public void addEvent(@RequestBody EventLog eventLog) {
        eventLogService.addEventLog(eventLog);
    }

    @RequestMapping(value = "/eventLogs/{logEntityId}", method = RequestMethod.GET)
    public List<EventLog> getEventLogsByLogEntityId(@PathVariable String logEntityId) {
        return eventLogService.getEventLogsByLogEntityId(logEntityId);
    }
}
