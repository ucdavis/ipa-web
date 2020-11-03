package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.ExpenseItemCategory;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ExpenseItemCategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.List;

@RestController
public class ExpenseItemCategoryController {
    @Inject ExpenseItemCategoryService expenseItemCategoryService;
    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/expenseItemTypes", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public List<ExpenseItemCategory> getExpenseItemTypes() {
        authorizer.isAuthorized();

        return expenseItemCategoryService.findAll();
    }
}
