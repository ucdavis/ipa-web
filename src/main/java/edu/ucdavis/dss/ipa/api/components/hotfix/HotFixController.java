package edu.ucdavis.dss.ipa.api.components.hotfix;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.TeachingCall;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.AuthenticationService;
import edu.ucdavis.dss.ipa.services.TeachingCallService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.utilities.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
public class HotFixController {
    private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.ipa");

    @Inject
    UserService userService;
    @Inject
    AuthenticationService authenticationService;
    @Inject
    TeachingCallService teachingCallService;

    @CrossOrigin // TODO: make CORS more specific depending on profile
    @RequestMapping(value = "/teachingCalls/#/{teachingCallId}", method = RequestMethod.GET)
    @ResponseBody
    public String oldTeachingCallUrlRedirect(@PathVariable Long teachingCallId, HttpServletResponse httpResponse) {
//        TeachingCall teachingCall = teachingCallService.findOneById(teachingCallId);
//
//        if(teachingCall == null) {
//            return "redirect:"
//        }
//
//        long workgroupId = teachingCall.getSchedule().getWorkgroup().getId();
//
//                		Calendar now = Calendar.getInstance();
//        		int academicYear = now.get(Calendar.YEAR);
//
//                		// example: February 2017 is for the academic year 2016, so currentYear would need to be decremented
//                        		if (now.get(Calendar.MONTH) < 7) {
//            			academicYear--;
//                        }
//
//                		String teachingCallUrl = SettingsConfiguration.getIpaURL() + "/assignments/" + workgroupId + "/" + academicYear + "/teachingCall";

        return "";
    }
}
