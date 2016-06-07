package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwAffiliation {
	private String name;

	public String getName() {
		return name;
	}
}