package edu.ucdavis.dss.ipa.exceptions;

import edu.ucdavis.dss.ipa.config.annotation.RestEndpointAdvice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import java.util.ArrayList;
import java.util.List;

@RestEndpointAdvice
public class RestExceptionHandler
{
	@SuppressWarnings("rawtypes")
	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handle(ConstraintViolationException e)
	{
		ErrorResponse errors = new ErrorResponse();
		for(ConstraintViolation violation : e.getConstraintViolations())
		{
			ErrorItem error = new ErrorItem();
			error.setCode(violation.getMessageTemplate());
			error.setMessage(violation.getMessage());
			errors.addError(error);
		}

		return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
	}

	public static class ErrorItem
	{
		private String code;
		private String message;

		@XmlAttribute
		public String getCode()
		{
			return code;
		}

		public void setCode(String code)
		{
			this.code = code;
		}

		@XmlValue
		public String getMessage()
		{
			return message;
		}

		public void setMessage(String message)
		{
			this.message = message;
		}
	}

	@XmlRootElement(name = "errors")
	public static class ErrorResponse
	{
		private List<ErrorItem> errors = new ArrayList<>();

		@XmlElement(name = "error")
		public List<ErrorItem> getErrors()
		{
			return errors;
		}

		public void setErrors(List<ErrorItem> errors)
		{
			this.errors = errors;
		}

		public void addError(ErrorItem error)
		{
			this.errors.add(error);
		}
	}
}
