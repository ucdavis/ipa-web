package edu.ucdavis.dss.ipa.api.entities;

import edu.ucdavis.dss.ipa.entities.ExpenseItem;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.Authorizer;
import edu.ucdavis.dss.ipa.services.ExpenseItemService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class ExpenseItemController {
    @Inject ExpenseItemService expenseItemService;
    @Inject WorkgroupService workgroupService;

    @Inject Authorizer authorizer;

    @RequestMapping(value = "/api/workgroups/{workgroupId}/years/{year}/expenseItems", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public List<ExpenseItem> getExpenseItems(@PathVariable long workgroupId,
                                       @PathVariable long year,
                                       HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        if (workgroup == null) {
            httpResponse.setStatus(HttpStatus.NOT_FOUND.value());
            return null;
        }

        authorizer.hasWorkgroupRoles(workgroup.getId(), "academicPlanner", "reviewer");
        List<ExpenseItem> expenseItems = expenseItemService.findbyWorkgroupIdAndYear(workgroupId, year);

        return expenseItems;
    }
}
