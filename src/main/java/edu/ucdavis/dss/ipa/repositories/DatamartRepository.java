package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.datamart.dto.DopeRecord;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import jakarta.inject.Inject;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@Profile({"development", "production", "staging"})
@ConditionalOnProperty(name = "DATAMART_URL")
@ConditionalOnNotWebApplication
public class DatamartRepository {
    @Inject EmailService emailService;
    @Inject @Qualifier("datamartJdbcTemplate") NamedParameterJdbcTemplate datamartJdbcTemplate;

    private static final String DOPE_SQL = """
        SELECT
          DOPE.EXPENSE_TYPE "Expense Type",
          CASE
            WHEN DOPE.EXPENSE_TYPE = 'FRINGE' AND DOPE.NATURAL_ACCOUNT IN (508300, 508301) THEN 'Vacation'
            WHEN DOPE.EXPENSE_TYPE = 'FRINGE' THEN 'Fringe'
            WHEN DOPE.EXPENSE_TYPE = 'SALARY' THEN DOPE.NATURAL_ACCOUNT
          END AS "Natural Account Grouping",
          DOPE.FISCAL_YEAR "Fiscal Year",
          DOPE.FISCAL_MONTH "Fiscal Month",
          DOPE.OPERATING_UNIT "Entity",
          DOPE.FUND_CODE "Fund",
          DOPE.DEPTID_CF "Financial Department",
          DEPT.DESCR "Financial Department Description",
          DOPE.UC_DEPTID_ROLLUP "Department Rollup",
          DOPE.NATURAL_ACCOUNT "Natural Account",
          DOPE.PURPOSE "Purpose",
          DOPE.PROGRAM "Program",
          DOPE.PROJECT "Project",
          DOPE.ACTIVITY "Activity",
          DOPE.TASK "Task",
          DOPE.AWARD "Award",
          DOPE.ALTACCT "Alt Acct",
          DOPE.TRANSACTION_ID "Transaction ID",
          DOPE.TXFR_FLAG "TXFR Flag",
          NAMES.NAME "Employee Name",
          DOPE.EMPLOYEE_ID "Employee ID",
          DOPE.EMPLOYEE_RECORD "Employee Record",
          DOPE.POSITION_NUMBER "Position Number",
          DOPE.DEPT_CD "Department Code",
          ORG.DEPT_TTL "Department Description",
          DOPE.JOBCODE "Job Code",
          JOBCODE.DESCR "Job Code Description",
          DOPE.PAY_END_DATE "Pay End Date",
          DOPE.UC_EARN_END_DATE "UC Earn End Date",
          DOPE.PAYCHECK_NUMBER "Paycheck Number",
          DOPE.EARN_CODE "Earn Code",
          DOPE.EMPL_CLASS "Employee Class",
          ECLASS.DESCR "Employee Class Description",
          DOPE.FTE "FTE",
          DOPE.HOURS "Hours",
          DOPE.HOURLY_RATE "Hourly Rate",
          DOPE.MONTHLY_RATE "Monthly Rate",
          DOPE.UC_PCT_TOTAL_PAY "UC Percent Total Pay",
          DOPE.UC_DRV_EFT_PCT "UC Derived Effort Percent",
          DOPE.MONETARY_AMOUNT "Monetary Amount"
        FROM
          LS_HCMODS.UCD_DM_DOPE_DATA_V DOPE
          JOIN LS_HCMODS.PS_DEPT_TBL_V DEPT ON DOPE.DEPTID_CF = DEPT.DEPTID AND SETID = 'DVFIN'
          JOIN LS_HCMODS.UCD_DM_PS_NAMES_PREF_V NAMES ON DOPE.EMPLOYEE_ID = NAMES.EMPLID
          JOIN LS_HCMODS.UCD_ORGANIZATION_D_V ORG on ORG.DEPT_CD = DOPE.DEPT_CD
          JOIN LS_HCMODS.UCD_DM_PS_JOBCODE_V JOBCODE ON DOPE.JOBCODE = JOBCODE.JOBCODE
          JOIN LS_HCMODS.UCD_DM_PS_EMPL_CLASS_V ECLASS ON DOPE.EMPL_CLASS = ECLASS.EMPL_CLASS
        WHERE 1=1
          AND DOPE.DEPT_CD = :departmentId
          AND DOPE.FISCAL_YEAR = :fiscalYear
        ORDER BY
          NAMES.NAME
        """;

    /**
     * Returns DOPE records from the Datamart for the given department
     * and fiscal year, or null on error.
     */
    public List<DopeRecord> getDopeRecords(String departmentId, int fiscalYear) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("departmentId", departmentId)
            .addValue("fiscalYear", fiscalYear);

        try {
            return datamartJdbcTemplate.query(DOPE_SQL, parameters, DatamartRepository::mapDopeRecord);
        } catch (Exception e) {
            emailService.reportException(e, this.getClass().getName());
            return null;
        }
    }

    /* which departments a set of employees were paid in for a fiscal year — no DEPT_CD filter, so it
       finds pay OUTSIDE the department being reported. LEFT JOIN the ORG view for a readable title,
       falling back to the dept code when a dept is absent from ORG (an inner join would silently drop
       those DOPE rows, misreporting a paid person as having no record). Bounded by employeeIds. */
    private static final String DEPARTMENTS_BY_EMPLOYEE_SQL = """
        SELECT DISTINCT DOPE.EMPLOYEE_ID "Employee ID", DOPE.DEPT_CD "Department Code",
               ORG.DEPT_TTL "Department Title"
        FROM LS_HCMODS.UCD_DM_DOPE_DATA_V DOPE
          LEFT JOIN LS_HCMODS.UCD_ORGANIZATION_D_V ORG ON ORG.DEPT_CD = DOPE.DEPT_CD
        WHERE DOPE.EMPLOYEE_ID IN (:employeeIds)
          AND DOPE.FISCAL_YEAR = :fiscalYear
        """;

    /**
     * For each given employee id, the set of departments they were paid in that fiscal year (across
     * all departments) — the readable title, or the dept code where the title is missing. Null on
     * error. An id absent from the result had no DOPE payroll that year.
     */
    public Map<String, Set<String>> getDepartmentsByEmployee(Set<String> employeeIds, int fiscalYear) {
        if (employeeIds.isEmpty()) {
            return Map.of();
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource()
            .addValue("employeeIds", employeeIds)
            .addValue("fiscalYear", fiscalYear);

        try {
            Map<String, Set<String>> departmentsByEmployee = new HashMap<>();
            datamartJdbcTemplate.query(DEPARTMENTS_BY_EMPLOYEE_SQL, parameters, rs -> {
                String title = rs.getString("Department Title");
                String department = title != null && !title.isBlank()
                    ? title : rs.getString("Department Code");
                departmentsByEmployee
                    .computeIfAbsent(rs.getString("Employee ID"), k -> new HashSet<>())
                    .add(department);
            });
            return departmentsByEmployee;
        } catch (Exception e) {
            emailService.reportException(e, this.getClass().getName());
            return null;
        }
    }

    static DopeRecord mapDopeRecord(ResultSet rs, int rowNum) throws SQLException {
        DopeRecord record = new DopeRecord();

        record.setExpenseType(rs.getString("Expense Type"));
        record.setNaturalAccountGrouping(rs.getString("Natural Account Grouping"));
        record.setFiscalYear(rs.getInt("Fiscal Year"));
        record.setFiscalMonth(rs.getInt("Fiscal Month"));
        record.setEntity(rs.getString("Entity"));
        record.setFund(rs.getString("Fund"));
        record.setFinancialDepartment(rs.getString("Financial Department"));
        record.setFinancialDepartmentDescription(rs.getString("Financial Department Description"));
        record.setDepartmentRollup(rs.getString("Department Rollup"));
        record.setNaturalAccount(rs.getString("Natural Account"));
        record.setPurpose(rs.getString("Purpose"));
        record.setProgram(rs.getString("Program"));
        record.setProject(rs.getString("Project"));
        record.setActivity(rs.getString("Activity"));
        record.setTask(rs.getString("Task"));
        record.setAward(rs.getString("Award"));
        record.setAltAcct(rs.getString("Alt Acct"));
        record.setTransactionId(rs.getString("Transaction ID"));
        record.setTxfrFlag(rs.getString("TXFR Flag"));
        record.setEmployeeName(rs.getString("Employee Name"));
        record.setEmployeeId(rs.getString("Employee ID"));
        record.setEmployeeRecord(rs.getInt("Employee Record"));
        record.setPositionNumber(rs.getString("Position Number"));
        record.setDepartmentCode(rs.getString("Department Code"));
        record.setDepartmentDescription(rs.getString("Department Description"));
        record.setJobCode(rs.getString("Job Code"));
        record.setJobCodeDescription(rs.getString("Job Code Description"));
        record.setPayEndDate(toLocalDate(rs.getDate("Pay End Date")));
        record.setUcEarnEndDate(toLocalDate(rs.getDate("UC Earn End Date")));
        record.setPaycheckNumber(rs.getString("Paycheck Number"));
        record.setEarnCode(rs.getString("Earn Code"));
        record.setEmployeeClass(rs.getString("Employee Class"));
        record.setEmployeeClassDescription(rs.getString("Employee Class Description"));
        record.setFte(rs.getBigDecimal("FTE"));
        record.setHours(rs.getBigDecimal("Hours"));
        record.setHourlyRate(rs.getBigDecimal("Hourly Rate"));
        record.setMonthlyRate(rs.getBigDecimal("Monthly Rate"));
        record.setUcPercentTotalPay(rs.getBigDecimal("UC Percent Total Pay"));
        record.setUcDerivedEffortPercent(rs.getBigDecimal("UC Derived Effort Percent"));
        record.setMonetaryAmount(rs.getBigDecimal("Monetary Amount"));

        return record;
    }

    private static java.time.LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }
}
