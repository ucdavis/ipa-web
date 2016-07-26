package edu.ucdavis.dss.ipa.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.ServletException;

/**
 * To be used when the cause of an error is an outage or unexpected behavior
 * from DSS Data Warehouse.
 */
@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class DwResponseException extends ServletException
{

}
