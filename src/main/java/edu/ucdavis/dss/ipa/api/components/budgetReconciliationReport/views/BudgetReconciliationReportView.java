package edu.ucdavis.dss.ipa.api.components.budgetReconciliationReport.views;

import java.util.List;

public class BudgetReconciliationReportView {
    long workgroupId;
    long year;
    int fiscalYear;
    String departmentCode;
    String budgetScenarioName;
    List<BudgetReconciliationCategoryView> categories;

    public BudgetReconciliationReportView(long workgroupId, long year, int fiscalYear,
                                          String departmentCode, String budgetScenarioName,
                                          List<BudgetReconciliationCategoryView> categories) {
        this.workgroupId = workgroupId;
        this.year = year;
        this.fiscalYear = fiscalYear;
        this.departmentCode = departmentCode;
        this.budgetScenarioName = budgetScenarioName;
        this.categories = categories;
    }

    public long getWorkgroupId() {
        return workgroupId;
    }

    public long getYear() {
        return year;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getBudgetScenarioName() {
        return budgetScenarioName;
    }

    public List<BudgetReconciliationCategoryView> getCategories() {
        return categories;
    }
}
