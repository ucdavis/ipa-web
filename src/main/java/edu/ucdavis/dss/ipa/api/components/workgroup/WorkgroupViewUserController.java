package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.api.helpers.CurrentUser;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.Instructor;
import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.Role;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import edu.ucdavis.dss.ipa.utilities.UserLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

@RestController
public class WorkgroupViewUserController {
    /**
     * Number of results to return when searching for people
     */
    private static final int PEOPLE_SEARCH_RESULT_LIMIT = 20;

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject WorkgroupService workgroupService;
    @Inject UserService userService;
    @Inject RoleService roleService;
    @Inject UserRoleService userRoleService;
    @Inject InstructorTypeService instructorTypeService;
    @Inject CurrentUser currentUser;
    @Inject DataWarehouseRepository dwRepository;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/users", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getUserRolesByWorkgroupCode(@PathVariable Long workgroupId) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

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
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

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
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

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
     * Search DW for people based on 'query'.
     *
     * Filters out users with null loginIds or emails.
     *
     * @param query
     * @return list of users
     */
    @RequestMapping(value = "/api/people/search", method = RequestMethod.GET)
    @ResponseBody
    public List<User> searchUsers(@RequestParam(value = "query", required = true) String query) {
        authorizer.isAuthorized();

        List<User> users = new ArrayList<User>();

        // Search both the general perosn search and the login ID-specific search.
        // If the login ID matches, it is placed at the top of the file
        List<DwPerson> dwPeople = dwRepository.searchPeople(query);

        if(dwPeople != null) {
            for (DwPerson dwPerson : dwPeople) {
                // Verify dwPerson has necessary data to make a User
                if (dwPerson.getUserId() != null && dwPerson.getEmail() != null) {
                    User user = new User();

                    user.setLoginId(dwPerson.getUserId());
                    user.setEmail(dwPerson.getEmail());
                    if (dwPerson.getdFirstName() != null && dwPerson.getdFirstName().length() > 0) {
                        user.setFirstName(dwPerson.getdFirstName());
                    } else {
                        // oFirstName is always full caps, ex: 'SMITH'
                        String firstName = Utilities.titleize(dwPerson.getoFirstName());
                        user.setFirstName(firstName);
                    }

                    if (dwPerson.getdLastName() != null && dwPerson.getdLastName().length() > 0) {
                        user.setLastName(dwPerson.getdLastName());
                    } else {
                        // oLastName is always full caps, ex: 'SMITH'
                        String lastName = Utilities.titleize(dwPerson.getoLastName());
                        user.setLastName(lastName);
                    }

                    users.add(user);
                }
            }
        }

        /**
         * Limit search results
         */
        if(users.size() > PEOPLE_SEARCH_RESULT_LIMIT) {
            users = users.subList(0, PEOPLE_SEARCH_RESULT_LIMIT);
        }

        return users;
    }

    @RequestMapping(value = "/api/workgroupView/workgroups/{workgroupId}/users", method = RequestMethod.POST)
    @ResponseBody
    public User createUser(@RequestBody User userDTO, @PathVariable Long workgroupId, HttpServletResponse httpResponse) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

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
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        if(userRoleService.deleteByLoginIdAndWorkgroupId(loginId, workgroupId)) {
            User user = this.userService.getOneByLoginId(loginId);
            UserLogger.log(currentUser, "Removed user " + user.getName() + " (" + loginId + ") from workgroup with ID " + workgroupId);
            httpResponse.setStatus(HttpStatus.OK.value());
        } else {
            httpResponse.setStatus(HttpStatus.NOT_ACCEPTABLE.value());
        }
    }

    @RequestMapping(value = "/api/workgroupView/workgroups/{workgroupId}/userRoles/{userRoleId}/instructorTypes/{instructorTypeId}", method = RequestMethod.PUT)
    @ResponseBody
    public UserRole setInstructorTypeOnUserRole(@PathVariable Long workgroupId, @PathVariable Long userRoleId, @PathVariable Long instructorTypeId, HttpServletResponse httpResponse) {
        authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        UserRole userRole = userRoleService.getOneById(userRoleId);
        InstructorType instructorType = instructorTypeService.findById(instructorTypeId);

        if (userRole == null || instructorType == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        userRole.setInstructorType(instructorType);

        return userRoleService.save(userRole);
    }

    @RequestMapping(value = "/api/workgroupView/userRoles/{userRoleId}/roles/{roleId}", method = RequestMethod.PUT)
    @ResponseBody
    public UserRole updateUserRole(@PathVariable Long userRoleId, @PathVariable Long roleId, HttpServletResponse httpResponse) {
        UserRole userRole = userRoleService.getOneById(userRoleId);
        Role role = roleService.findOneById(roleId);

        if (userRole == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRole(userRole.getWorkgroup().getId(), "academicPlanner");

        userRole.setRole(role);

        return userRoleService.save(userRole);
    }

    @RequestMapping(value = "/api/workgroups/{workgroupId}/users/placeholder", method = RequestMethod.POST)
    @ResponseBody
    public User createPlaceholderUser(@PathVariable Long workgroupId, @RequestBody User user, HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        return userService.createPlaceholder(user);
    }

    @RequestMapping(value = "/api/workgroups/{workgroupId}/users/placeholder/{previousLoginId}", method = RequestMethod.PUT)
    @ResponseBody
    public User updatePlaceholder(@PathVariable Long workgroupId, @PathVariable String previousLoginId, @RequestBody User user, HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRole(workgroup.getId(), "academicPlanner");

        return userService.updatePlaceholder(previousLoginId, user);
    }
}
