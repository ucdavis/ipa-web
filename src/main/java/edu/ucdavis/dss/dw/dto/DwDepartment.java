package edu.ucdavis.dss.dw.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;

import edu.ucdavis.dss.ipa.web.views.WorkgroupViews;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DwDepartment {
	private long id;
	private String name, code, number;

	@JsonView(WorkgroupViews.Summary.class)
	public long getId() {
		return id;
	}
	
	@JsonView(WorkgroupViews.Summary.class)
	public String getName() {
		return name;
	}
	
	@JsonView(WorkgroupViews.Summary.class)
	public String getCode() {
		return code;
	}
	
	@JsonView(WorkgroupViews.Summary.class)
	public String getNumber() {
		return number;
	}

}
