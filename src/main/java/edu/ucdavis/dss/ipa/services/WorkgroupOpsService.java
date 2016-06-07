package edu.ucdavis.dss.ipa.services;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.Workgroup;

@Validated
public interface WorkgroupOpsService {
	/**
	 * Performs all steps necessary to create an entirely new workgroup.
	 * 
	 * Includes importing users, historical schedules+schedule term states.
	 * 
	 * @param workgroup
	 * @return
	 */
	Workgroup provisionNewWorkgroup(@Valid Workgroup workgroup);

	/**
	 * Completely removes a workgroup and its corresponding data (COGs, tracks, etc.)
	 * 
	 * @param workgroupId
	 * @return
	 */
	boolean deleteWorkgroup(long workgroupId);
}
