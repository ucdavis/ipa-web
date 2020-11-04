package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.ExpenseItemType;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ExpenseItemTypeService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
public class ExpenseItemTypeController {
    @Inject ExpenseItemTypeService ExpenseItemTypeService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/expenseItemTypes", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public List<ExpenseItemType> getExpenseItemTypes() {
        authorizer.isAuthorized();

        return ExpenseItemTypeService.findAll();
    }
}
