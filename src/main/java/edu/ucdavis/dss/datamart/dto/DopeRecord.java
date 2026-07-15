package edu.ucdavis.dss.datamart.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of Distribution of Payroll Expense (DOPE) data from the Datamart.
 */
public class DopeRecord {
    String expenseType, naturalAccountGrouping, entity, fund, financialDepartment,
        financialDepartmentDescription, departmentRollup, naturalAccount, purpose, program,
        project, activity, task, award, altAcct, transactionId, txfrFlag, employeeName,
        employeeId, positionNumber, departmentCode, departmentDescription, jobCode,
        jobCodeDescription, paycheckNumber, earnCode, employeeClass, employeeClassDescription;
    int fiscalYear, fiscalMonth, employeeRecord;
    LocalDate payEndDate, ucEarnEndDate;
    BigDecimal fte, hours, hourlyRate, monthlyRate, ucPercentTotalPay, ucDerivedEffortPercent,
        monetaryAmount;

    public String getExpenseType() {
        return expenseType;
    }

    public void setExpenseType(String expenseType) {
        this.expenseType = expenseType;
    }

    public String getNaturalAccountGrouping() {
        return naturalAccountGrouping;
    }

    public void setNaturalAccountGrouping(String naturalAccountGrouping) {
        this.naturalAccountGrouping = naturalAccountGrouping;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getFund() {
        return fund;
    }

    public void setFund(String fund) {
        this.fund = fund;
    }

    public String getFinancialDepartment() {
        return financialDepartment;
    }

    public void setFinancialDepartment(String financialDepartment) {
        this.financialDepartment = financialDepartment;
    }

    public String getFinancialDepartmentDescription() {
        return financialDepartmentDescription;
    }

    public void setFinancialDepartmentDescription(String financialDepartmentDescription) {
        this.financialDepartmentDescription = financialDepartmentDescription;
    }

    public String getDepartmentRollup() {
        return departmentRollup;
    }

    public void setDepartmentRollup(String departmentRollup) {
        this.departmentRollup = departmentRollup;
    }

    public String getNaturalAccount() {
        return naturalAccount;
    }

    public void setNaturalAccount(String naturalAccount) {
        this.naturalAccount = naturalAccount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getAward() {
        return award;
    }

    public void setAward(String award) {
        this.award = award;
    }

    public String getAltAcct() {
        return altAcct;
    }

    public void setAltAcct(String altAcct) {
        this.altAcct = altAcct;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTxfrFlag() {
        return txfrFlag;
    }

    public void setTxfrFlag(String txfrFlag) {
        this.txfrFlag = txfrFlag;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPositionNumber() {
        return positionNumber;
    }

    public void setPositionNumber(String positionNumber) {
        this.positionNumber = positionNumber;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
    }

    public String getDepartmentDescription() {
        return departmentDescription;
    }

    public void setDepartmentDescription(String departmentDescription) {
        this.departmentDescription = departmentDescription;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobCodeDescription() {
        return jobCodeDescription;
    }

    public void setJobCodeDescription(String jobCodeDescription) {
        this.jobCodeDescription = jobCodeDescription;
    }

    public String getPaycheckNumber() {
        return paycheckNumber;
    }

    public void setPaycheckNumber(String paycheckNumber) {
        this.paycheckNumber = paycheckNumber;
    }

    public String getEarnCode() {
        return earnCode;
    }

    public void setEarnCode(String earnCode) {
        this.earnCode = earnCode;
    }

    public String getEmployeeClass() {
        return employeeClass;
    }

    public void setEmployeeClass(String employeeClass) {
        this.employeeClass = employeeClass;
    }

    public String getEmployeeClassDescription() {
        return employeeClassDescription;
    }

    public void setEmployeeClassDescription(String employeeClassDescription) {
        this.employeeClassDescription = employeeClassDescription;
    }

    public int getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(int fiscalYear) {
        this.fiscalYear = fiscalYear;
    }

    public int getFiscalMonth() {
        return fiscalMonth;
    }

    public void setFiscalMonth(int fiscalMonth) {
        this.fiscalMonth = fiscalMonth;
    }

    public int getEmployeeRecord() {
        return employeeRecord;
    }

    public void setEmployeeRecord(int employeeRecord) {
        this.employeeRecord = employeeRecord;
    }

    public LocalDate getPayEndDate() {
        return payEndDate;
    }

    public void setPayEndDate(LocalDate payEndDate) {
        this.payEndDate = payEndDate;
    }

    public LocalDate getUcEarnEndDate() {
        return ucEarnEndDate;
    }

    public void setUcEarnEndDate(LocalDate ucEarnEndDate) {
        this.ucEarnEndDate = ucEarnEndDate;
    }

    public BigDecimal getFte() {
        return fte;
    }

    public void setFte(BigDecimal fte) {
        this.fte = fte;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public BigDecimal getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(BigDecimal hourlyRate) {
        this.hourlyRate = hourlyRate;
    }

    public BigDecimal getMonthlyRate() {
        return monthlyRate;
    }

    public void setMonthlyRate(BigDecimal monthlyRate) {
        this.monthlyRate = monthlyRate;
    }

    public BigDecimal getUcPercentTotalPay() {
        return ucPercentTotalPay;
    }

    public void setUcPercentTotalPay(BigDecimal ucPercentTotalPay) {
        this.ucPercentTotalPay = ucPercentTotalPay;
    }

    public BigDecimal getUcDerivedEffortPercent() {
        return ucDerivedEffortPercent;
    }

    public void setUcDerivedEffortPercent(BigDecimal ucDerivedEffortPercent) {
        this.ucDerivedEffortPercent = ucDerivedEffortPercent;
    }

    public BigDecimal getMonetaryAmount() {
        return monetaryAmount;
    }

    public void setMonetaryAmount(BigDecimal monetaryAmount) {
        this.monetaryAmount = monetaryAmount;
    }
}
