package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.InstructorType;
import edu.ucdavis.dss.ipa.entities.LineItemCategory;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.InstructorTypeService;
import edu.ucdavis.dss.ipa.services.LineItemCategoryService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
@CrossOrigin
public class LineItemCategoryController {
  @Inject LineItemCategoryService lineItemCategoryService;
  @Inject Authorizer authorizer;

  @RequestMapping(value = "/api/lineItemCategories", method = RequestMethod.GET, produces="application/json")
  @ResponseBody
  public List<LineItemCategory> getLineItemCategories() {
    authorizer.isAuthorized();

    return lineItemCategoryService.findAll();
  }
}
