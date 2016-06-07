package edu.ucdavis.dss.ipa.exceptions;

@SuppressWarnings("serial")
public class ScheduleAlreadyExistsException extends Exception {
	public ScheduleAlreadyExistsException(String message) {
		super(message);
	}
}
