package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
@Profile({"production", "staging"})
public class UpdateUsersTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private static final Logger log = LoggerFactory.getLogger("UpdateUsersTask");

    @Inject DataWarehouseRepository dataWarehouseRepository;
    @Inject UserService userService;

    /**
     * Ensures users have an IAM ID and will update the displayName if a new one is found
     */
    @Scheduled( fixedDelay = 604800000 ) // every 7 days
    @Async
    public void UpdateUsersTask() {
        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        List<User> users = this.userService.getAllUsers();

        for (User user : users) {
            DwPerson dwPerson = dataWarehouseRepository.getPersonByLoginId(user.getLoginId());

            if (dwPerson == null) {
                log.debug("Unable to update user in task: DW returned null for login ID " + user.getLoginId());
                continue;
            }

            String iamId = dwPerson.getIamId();
            String firstName = dwPerson.getdFirstName();
            String lastName = dwPerson.getdLastName();
            String displayName = dwPerson.getdFullName();
            String email = dwPerson.getEmail();

            if (displayName != null && displayName.length() > 0) {
                user.setDisplayName(displayName);
            }

            if (firstName != null && firstName.length() > 0) {
                user.setFirstName(firstName);
            }

            if (lastName != null && lastName.length() > 0) {
                user.setLastName(lastName);
            }

            if (iamId != null && iamId.length() > 0) {
                user.setIamId(Long.valueOf(iamId));
            }

            if (email != null && email.length() > 0) {
                user.setEmail(email);
            }

            userService.save(user);
        }

        runningTask = false;
    }
}
