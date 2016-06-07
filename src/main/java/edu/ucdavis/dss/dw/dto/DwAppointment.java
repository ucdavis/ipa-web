package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwAppointment {
	private DwAffiliation affiliation;
	private DwDepartment department;

	public DwDepartment getDepartment() {
		return department;
	}
	
	public DwAffiliation getAffiliation() {
		return affiliation;
	}
	
	// Used for mapping between DataWarehouse freeform text affiliations and IPA Roles
	@JsonIgnore
	public String getRoleEquivalent() {
		String name = affiliation.getName();
		
		switch (name) {
			case "staff":
			case "staff:career":
				return "academicCoordinator";
			case "faculty:senate":
			case "faculty:federation":
			case "faculty":
				return "instructor";
			default:
				return null;
		}
	}
}