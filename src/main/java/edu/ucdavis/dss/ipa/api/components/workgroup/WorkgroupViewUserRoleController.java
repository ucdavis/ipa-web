package edu.ucdavis.dss.ipa.api.components.workgroup;

import com.fasterxml.jackson.annotation.JsonView;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.views.UserViews;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.UserLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewUserRoleController {

    private static final Logger log = LogManager.getLogger();

    @Inject WorkgroupService workgroupService;
    @Inject UserService userService;
    @Inject RoleService roleService;
    @Inject UserRoleService userRoleService;
    @Inject CurrentUser currentUser;

    @PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/users", method = RequestMethod.GET)
    @ResponseBody
    @JsonView(UserViews.Detailed.class)
    public List<User> getUserRolesByWorkgroupCode(@PathVariable String workgroupCode, HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);

        List<User> users = new ArrayList<User>();

        for (UserRole userRole : workgroup.getUserRoles()) {
            if (!users.contains(userRole.getUser())) {
                users.add(userRole.getUser());
            }
        }

        return users;
    }

    @RequestMapping(value = "/api/workgroupView/users/{loginId}/workgroups/{workgroupCode}/roles/{role}", method = RequestMethod.POST)
    @ResponseBody
    @PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
    public UserRole addUserRoleToUser(@PathVariable String loginId, @PathVariable String workgroupCode, @PathVariable String role, HttpServletResponse httpResponse) {
        User user = userService.findOrCreateByLoginId(loginId);
        Role newRole = roleService.findOneByName(role);

        //TODO: Ideally this scenario should be handled by a more nuanced hasPermission method
        // Only Admins can modify the admin role
        if ("admin".equals(role)) {
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return null;
        }

        // Is the role invalid?
        UserRole userRole = null;
        if (newRole == null) {
            log.error("Attempted to add userRole failed: role was invalid.");
            // Has the user been added to IPA already?
            httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        }
        else {
            userRole = userRoleService.findOrCreateByLoginIdAndWorkgroupCodeAndRoleToken(loginId, workgroupCode, role);
            httpResponse.setStatus(HttpStatus.OK.value());
            UserLogger.log(currentUser, "Added role '" + role + "' to user " + user.getName() + " (" + loginId + ")");
        }

        // query new roleList and return

        return userRole;
    }

    @RequestMapping(value = "/api/workgroupView/users/{loginId}/workgroups/{workgroupCode}/roles/{role}", method = RequestMethod.DELETE)
    @ResponseBody
    @PreAuthorize("hasPermission(#workgroupId, 'workgroup', 'academicCoordinator')")
    public void removeUserRoleFromUser(@PathVariable String loginId, @PathVariable String workgroupCode, @PathVariable String role, HttpServletResponse httpResponse) {
        Role newRole = roleService.findOneByName(role);
        User user = this.userService.getOneByLoginId(loginId);

        //TODO: Ideally this scenario should be handled by a more nuanced hasPermission method
        // Only Admins can modify the admin role
        if ("admin".equals(role) && !user.isAdmin()) {
            httpResponse.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }

        // Is the role invalid?
        if (newRole == null) {
            httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        } else {
            userRoleService.deleteByLoginIdAndWorkgroupCodeAndRoleToken(loginId, workgroupCode, role);

            // Query new roleList and return
            user = this.userService.getOneByLoginId(loginId);
            httpResponse.setStatus(HttpStatus.OK.value());
            UserLogger.log(currentUser, "Removed role '" + role + "' from user " + user.getName() + " (" + loginId + ")");
        }

    }

}