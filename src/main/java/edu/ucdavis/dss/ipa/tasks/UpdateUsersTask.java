package edu.ucdavis.dss.ipa.tasks;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.repositories.RestDataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.UserService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;

@Service
public class UpdateUsersTask {
    private static boolean runningTask = false; /* flag to avoid multiple concurrent tasks */

    @Inject
    RestDataWarehouseRepository restDataWarehouseRepository;
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
            DwPerson dwPerson = restDataWarehouseRepository.getPersonByLoginId(user.getLoginId());

            if (dwPerson == null) {
                continue;
            }

            String iamId = dwPerson.getIamId();
            String firstName = dwPerson.getdFirstName();
            String lastName = dwPerson.getdLastName();
            String displayName = dwPerson.getdFullName()
            if (firstName != null && firstName.length() > 0) {
                user.setFirstName(firstName);
            }

            if (lastName != null && lastName.length() > 0) {
                user.setLastName(lastName);
            }

            if (iamId != null && iamId.length() > 0) {
                user.setIamId(Long.valueOf(iamId));
            }

            userService.save(user);
        }

        runningTask = false;
    }
}
