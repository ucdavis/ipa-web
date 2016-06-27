package edu.ucdavis.dss.ipa.security.authorization;

import edu.ucdavis.dss.ipa.entities.Tag;
import org.springframework.stereotype.Service;

/**
 * Created by okadri on 6/22/16.
 */
@Service
public class TagAuthorizer implements Authorizer<Tag> {

    /**
     * Asserts that a given user has role 'academicPlanner' in the same workgroup as the tag
     * throws an exception otherwise
     * @param entity Tag
     * @param args Long (workgroupId)
     * @return
     */
    @Override
    public void authorize(Tag entity, Object... args) {
//        if (args.length != 1) {
//            throw new AccessDeniedException("Incorrect parameters, expected workgroupId");
//        }
//
//        if (args[0] instanceof Long == false) {
//            throw new AccessDeniedException("Incorrect parameter, expected a Long");
//        }
//
//        Long workgroupId = (Long) args[0];
//        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");
    }

}
