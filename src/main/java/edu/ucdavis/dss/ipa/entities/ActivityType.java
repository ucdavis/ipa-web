package edu.ucdavis.dss.ipa.entities;

import javax.persistence.Embeddable;

import org.springframework.data.annotation.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.ucdavis.dss.ipa.api.views.SectionGroupViews;

/**
 * Information provided by the UCD Registrar.
 * Modeled after DW implementation
 * @author Christopher Thielen
 *
 */
@Embeddable
public final class ActivityType {
	private char activityTypeCode;
	
	public ActivityType(char code) {
		this.activityTypeCode = Character.toUpperCase(code);
	}

	public ActivityType() {
	}

	public char getActivityTypeCode() {
		return this.activityTypeCode;
	}
	
	public void setActivityTypeCode(char activityTypeCode) {
		this.activityTypeCode = activityTypeCode;
	}
	
	@Transient
	public String GetDescription(char activityTypeCode) {
		String description = null;
		
		switch(activityTypeCode) {
		case '%':
			description = "World Wide Web Electronic Discussion";
			break;
		case '0':
			description = "World Wide Web Virtual Lecture";
			break;
		case '1':
			description = "Conference";
			break;
		case '2':
			description = "Term Paper/Discussion";
			break;
		case '3':
			description = "Film Viewing";
			break;
		case '6':
			// Not for use by departments
			description = "Dummy Course";
			break;
		case '7':
			// Course with more than one activity
			description = "Combined Schedule";
			break;
		case '8':
			description = "Project";
			break;
		case '9':
			description = "Extensive Writing or Discussion";
			break;
		case 'A':
			description = "Lecture";
			break;
		case 'B':
			description = "Lecture/Discussion";
			break;
		case 'C':
			description = "Laboratory";
			break;
		case 'D':
			description = "Discussion";
			break;
		case 'E':
			description = "Seminar";
			break;
		case 'F':
			description = "Fieldwork";
			break;
		case 'G':
			description = "Discussion/Laboratory";
			break;
		case 'H':
			description = "Laboratory/Discussion";
			break;
		case 'I':
			description = "Internship";
			break;
		case 'J':
			description = "Independent Study";
			break;
		case 'K':
			description = "Workshop";
			break;
		case 'L':
			description = "Lecture/Lab";
			break;
		case 'O':
			description = "Clinic";
			break;
		case 'P':
			description = "PE Activity";
			break;
		case 'Q':
			description = "Listening";
			break;
		case 'R':
			description = "Recitation";
			break;
		case 'S':
			description = "Studio";
			break;
		case 'T':
			description = "Tutorial";
			break;
		case 'U':
			description = "Auto Tutorial";
			break;
		case 'V':
			description = "Variable";
			break;
		case 'W':
			description = "Practice";
			break;
		case 'X':
			description = "Performance Instruction";
			break;
		case 'Y':
			description = "Rehearsal";
			break;
		case 'Z':
			description = "Term Paper";
			break;
		}
		
		return description;
	}
	
	@Transient
	@JsonIgnore
	public String GetAbbreviation(char activityTypeCode) {
		String abbreviation = null;
		
		switch(activityTypeCode) {
		case '%':
			abbreviation = "WED";
			break;
		case '0':
			abbreviation = "WVL";
			break;
		case '1':
			abbreviation = "CON";
			break;
		case '2':
			abbreviation = "T-D";
			break;
		case '3':
			abbreviation = "F-V";
			break;
		case '6':
			// Not for use by departments
			abbreviation = "DUM";
			break;
		case '7':
			abbreviation = "COM";
			break;
		case '8':
			abbreviation = "PRJ";
			break;
		case '9':
			abbreviation = "W-D";
			break;
		case 'A':
			abbreviation = "LEC";
			break;
		case 'B':
			abbreviation = "LED";
			break;
		case 'C':
			abbreviation = "LAB";
			break;
		case 'D':
			abbreviation = "DIS";
			break;
		case 'E':
			abbreviation = "SEM";
			break;
		case 'F':
			abbreviation = "FWK";
			break;
		case 'G':
			abbreviation = "D/L";
			break;
		case 'H':
			abbreviation = "L/D";
			break;
		case 'I':
			abbreviation = "INT";
			break;
		case 'J':
			abbreviation = "IND";
			break;
		case 'K':
			abbreviation = "WRK";
			break;
		case 'L':
			abbreviation = "LLA";
			break;
		case 'O':
			abbreviation = "CLI";
			break;
		case 'P':
			abbreviation = "ACT";
			break;
		case 'Q':
			abbreviation = "LIS";
			break;
		case 'R':
			abbreviation = "REC";
			break;
		case 'S':
			abbreviation = "STD";
			break;
		case 'T':
			abbreviation = "TUT";
			break;
		case 'U':
			abbreviation = "AUT";
			break;
		case 'V':
			abbreviation = "VAR";
			break;
		case 'W':
			abbreviation = "PRA";
			break;
		case 'X':
			abbreviation = "PER";
			break;
		case 'Y':
			abbreviation = "REH";
			break;
		case 'Z':
			abbreviation = "TMP";
			break;
		}
		
		return abbreviation;
	}
}