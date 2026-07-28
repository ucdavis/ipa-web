/* UCPath payroll department code (DOPE DEPT_CD, e.g. '040250') the workgroup's instructional payroll
   lands in. NULL means the workgroup is outside the Letters & Science scope of the budget
   reconciliation report, or has not been mapped yet. Many-to-one: workgroups sharing a payroll
   department (e.g. German and Russian both under 040170) carry the same code, so no unique index.
   Not to be confused with WorkgroupCode, the 4-char subject-style abbreviation that was itself named
   DepartmentCode before V59. */
ALTER TABLE Workgroups ADD COLUMN DepartmentCode VARCHAR(6) DEFAULT NULL AFTER WorkgroupCode;
