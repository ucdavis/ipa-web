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
@Profile({"production", "staging", "development"})
public class UpdateUsersTask {
    final long ONE_WEEK_IN_MILLISECONDS = 604800000;
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */
    private static final Logger log = LoggerFactory.getLogger("UpdateUsersTask");

    @Inject DataWarehouseRepository dataWarehouseRepository;
    @Inject UserService userService;

    /**
     * Ensures users have an IAM ID and will update the displayName if a new one is found
     */
    @Scheduled( fixedDelay = ONE_WEEK_IN_MILLISECONDS )
    @Async
    public void updateUsersFromDW() {
        if(runningTask) return; // avoid multiple concurrent jobs
        runningTask = true;

        log.debug("updateUsersFromDW() started");

        List<User> users = this.userService.getAllUsers();

        for (User user : users) {
            DwPerson dwPerson = dataWarehouseRepository.getPersonByLoginId(user.getLoginId());

            if (dwPerson == null) {
                // Person may have left the university. This is not necessarily an issue.
                continue;
            }

            String displayName = dwPerson.getdFullName();
            if (displayName != null && displayName.length() > 0) {
                user.setDisplayName(displayName);
            }

            String firstName = dwPerson.getdFirstName();
            if (firstName != null && firstName.length() > 0) {
                user.setFirstName(firstName);
            }

            String lastName = dwPerson.getdLastName();
            if (lastName != null && lastName.length() > 0) {
                user.setLastName(lastName);
            }

            String iamId = dwPerson.getIamId();
            if (iamId != null && iamId.length() > 0) {
                user.setIamId(Long.valueOf(iamId));
            }

            String email = dwPerson.getEmail();
            if (email != null && email.length() > 0) {
                user.setEmail(email);
            }

            userService.save(user);
        }

        log.debug("updateUsersFromDW() finished");

        runningTask = false;
    }
}
