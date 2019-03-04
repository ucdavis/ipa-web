package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
public class InstructorTypeController {
  @Inject InstructorTypeService instructorTypeService;
  @Inject Authorizer authorizer;

  @RequestMapping(value = "/api/instructorTypes", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<InstructorType> getInstructorTypes() {
    authorizer.isAuthorized();

    return instructorTypeService.getAllInstructorTypes();
  }
}
