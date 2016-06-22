package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.utilities.UserLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewUserController {

    private static final Logger log = LogManager.getLogger();

    @Inject WorkgroupService workgroupService;
    @Inject UserService userService;
    @Inject RoleService roleService;
    @Inject UserRoleService userRoleService;
    @Inject CurrentUser currentUser;
    @Inject DataWarehouseRepository dwRepository;

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/users", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getUserRolesByWorkgroupCode(@PathVariable Long workgroupId, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicCoordinator");

        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        List<User> users = new ArrayList<User>();

        for (UserRole userRole : workgroup.getUserRoles()) {
            if (!users.contains(userRole.getUser())) {
                users.add(userRole.getUser());
            }
        }

        return users;
    }

    @RequestMapping(value = "/api/workgroupView/users/{loginId}/workgroups/{workgroupId}/roles/{role}", method = RequestMethod.POST)
    @ResponseBody
    public UserRole addUserRoleToUser(@PathVariable String loginId, @PathVariable Long workgroupId, @PathVariable String role, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicCoordinator");

        User user = userService.getOneByLoginId(loginId);

        if (user == null) {
            httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            return null;
        }

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
            userRole = userRoleService.findOrCreateByLoginIdAndWorkgroupIdAndRoleToken(loginId, workgroupId, role);
            httpResponse.setStatus(HttpStatus.OK.value());
            UserLogger.log(currentUser, "Added role '" + role + "' to user " + user.getName() + " (" + loginId + ")");
        }

        // query new roleList and return

        return userRole;
    }

    @RequestMapping(value = "/api/workgroupView/users/{loginId}/workgroups/{workgroupId}/roles/{role}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeUserRoleFromUser(@PathVariable String loginId, @PathVariable Long workgroupId, @PathVariable String role, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicCoordinator");

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
            userRoleService.deleteByLoginIdAndWorkgroupIdAndRoleToken(loginId, workgroupId, role);

            // Query new roleList and return
            user = this.userService.getOneByLoginId(loginId);
            httpResponse.setStatus(HttpStatus.OK.value());
            UserLogger.log(currentUser, "Removed role '" + role + "' from user " + user.getName() + " (" + loginId + ")");
        }

    }

    /**
     * Accepts a query string,
     * returns a list of users from data warehouse.
     * Filters out users with null loginIds or emails,
     * and users who already have a role in the specified workgroup.
     * @param workgroupId
     * @param query
     * @param httpResponse
     * @return
     */
    @RequestMapping(value = "/api/workgroupView/workgroups/{workgroupId}/userSearch", method = RequestMethod.GET)
    @ResponseBody
    public List<User> searchUsers(
            @PathVariable Long workgroupId,
            @RequestParam(value = "query", required = true) String query, HttpServletResponse httpResponse) {

        Authorizer.hasWorkgroupRole(workgroupId, "academicCoordinator");

        List<User> users = new ArrayList<User>();
        List<DwPerson> dwPeople;

        try {
            dwPeople = dwRepository.searchPeople(query);

            for (DwPerson dwPerson : dwPeople) {
                // Verify dwPerson has necessary data to make a User
                if (dwPerson.getLoginId() != null && dwPerson.getEmail() != null) {
                    boolean userBelongsToWorkgroup = workgroupService.hasUser(workgroupId, dwPerson.getLoginId());

                    if (userBelongsToWorkgroup == false) {
                        User user = new User();
                        user.setLoginId(dwPerson.getLoginId());
                        user.setEmail(dwPerson.getEmail());
                        user.setFirstName(dwPerson.getFirst());
                        user.setLastName(dwPerson.getLast());
                        users.add(user);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionLogger.logAndMailException(this.getClass().getName(), e);
        }

        return users;
    }

    @RequestMapping(value = "/api/workgroupView/workgroups/{workgroupId}/users", method = RequestMethod.POST)
    @ResponseBody
    public User createUser(@RequestBody User userDTO, @PathVariable Long workgroupId, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicCoordinator");

        User user = userService.findOrCreateByLoginId(userDTO.getLoginId());

        if (user == null) {
            httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
            return null;
        }

        return user;
    }


    @RequestMapping(value = "/api/workgroupView/workgroups/{workgroupId}/users/{loginId}", method = RequestMethod.DELETE)
    @ResponseBody
    public void removeUserFromWorkgroup(@PathVariable String loginId, @PathVariable Long workgroupId, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicCoordinator");

        if(userRoleService.deleteByLoginIdAndWorkgroupId(loginId, workgroupId)) {
            User user = this.userService.getOneByLoginId(loginId);
            UserLogger.log(currentUser, "Removed user " + user.getName() + " (" + loginId + ") from workgroup with ID " + workgroupId);
            httpResponse.setStatus(HttpStatus.OK.value());
        } else {
            httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        }
    }

}