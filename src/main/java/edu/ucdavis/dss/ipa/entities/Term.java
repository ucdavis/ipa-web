package edu.ucdavis.dss.ipa.entities;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings("serial")
@Entity
@Table(name = "Terms")
public class Term implements Serializable {
	private String termCode;
	private Date bannerStartWindow1, bannerEndWindow1,
		bannerStartWindow2, bannerEndWindow2, startDate, endDate;

	@Id
	@Column(name = "TermCode", unique = true, nullable = false)
	@JsonProperty
	public String getTermCode() {
		return this.termCode;
	}

	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}
	
	@Basic
	@Column(name = "BannerStartWindow1", unique = true, nullable = true)
	@JsonProperty
	public Date getBannerStartWindow1() {
		return bannerStartWindow1;
	}

	public void setBannerStartWindow1(Date bannerStartWindow) {
		this.bannerStartWindow1 = bannerStartWindow;
	}
	
	@Basic
	@Column(name = "BannerEndWindow1", unique = true, nullable = true)
	@JsonProperty
	public Date getBannerEndWindow1() {
		return bannerEndWindow1;
	}

	public void setBannerEndWindow1(Date bannerEndWindow) {
		this.bannerEndWindow1 = bannerEndWindow;
	}

	@Basic
	@Column(name = "BannerStartWindow2", unique = true, nullable = true)
	@JsonProperty
	public Date getBannerStartWindow2() {
		return bannerStartWindow2;
	}

	public void setBannerStartWindow2(Date bannerStartWindow2) {
		this.bannerStartWindow2 = bannerStartWindow2;
	}

	@Basic
	@Column(name = "BannerEndWindow2", unique = true, nullable = true)
	@JsonProperty
	public Date getBannerEndWindow2() {
		return bannerEndWindow2;
	}

	public void setBannerEndWindow2(Date bannerEndWindow2) {
		this.bannerEndWindow2 = bannerEndWindow2;
	}

	@Basic
	@Column(name = "StartDate", unique = true, nullable = true)
	@JsonProperty
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Basic
	@Column(name = "EndDate", unique = true, nullable = true)
	@JsonProperty
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	/**
	 * Provide all term codes for an academic year.
	 *
	 * @param year first year in the academic year, e.g. for "2013-14", provide 2013
	 * @return     list of all possible term codes for the given academic year
	 */
	@Transient
	public static Set<String> getTermCodesByYear(long year) {
		Set<String> termCodes = new HashSet<String>();

		for(int i = 5; i < 10; i++) {
			termCodes.add(Long.toString(year) + "0" + i);
		}
		termCodes.add(Long.toString(year) + "10");

		for(int i = 1; i < 5; i++) {
			termCodes.add(Long.toString(year + 1) + "0" + i);
		}

		return termCodes;
	}
	
	/**
	 * Provide the two digit code of the provided termCode
	 *
	 * @param termCode e.g. for "201510"
	 * @return         2 digit string, e.g. "10"
	 */
	@Transient
	public static String getTwoDigitTermCode(String termCode) {
		return termCode.substring(Math.max(termCode.length() - 2, 0));
	}
	
	/**
	 * Returns the equivalent termCode for the provided year
	 *
	 * @param year e.g. 2016
	 * @param termCode e.g. "201301"
	 * @return         "201701"
	 */
	@Transient
	public static String getTermCodeByYearAndTermCode(long year, String termCode) {
		String twoDigitTermCode =  Term.getTwoDigitTermCode(termCode);

		if (Integer.parseInt(twoDigitTermCode) < 5) {
			year++;
		}

		return year + twoDigitTermCode;
	}

	/**
	 * Returns the term name based solely on the code, ignoring the
	 * 'name' field in the database.
	 * <p>
	 * Names are based on the information found at:
	 * http://registrar.ucdavis.edu/faculty-staff/scheduling-guide/codes.cfm
	 * 
	 * @return the UCD Registrar name for this.code
	 */
	@Transient
	static public String getRegistrarName(String termCode) {
		if(termCode == null) throw new IllegalArgumentException("termCode cannot be null");

		String term = termCode.length() == 2 ? termCode : termCode.substring(4);
		int code = Integer.parseInt(term);
		
		switch(code) {
			case 1: return "Winter Quarter"; 
			case 2: return "Spring Semester";
			case 3: return "Spring Quarter";
			case 4: return "Unused";
			case 5: return "Summer Session 1";
			case 6: return "Summer Special Session";
			case 7: return "Summer Session 2";
			case 8: return "Summer Quarter";
			case 9: return "Fall Semester";
			case 10: return "Fall Quarter";
			default: return "Unknown";
		}
	}

	@Transient
	public static String getYear(String termCode) {
		if(termCode == null) throw new IllegalArgumentException("termCode cannot be null");
		if(termCode.length() != 6) throw new IllegalArgumentException("Cannot get year if termCode is short");

		return termCode.substring(0, 4);
	}
}