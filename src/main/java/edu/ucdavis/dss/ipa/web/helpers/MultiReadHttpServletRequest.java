package edu.ucdavis.dss.ipa.web.helpers;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Needed by the MvcExceptionHandler to read JSON requests to log the body for
 * exception-causing requests. Normally the request body cannot be read more than
 * once, so after Spring processes the body, we would be unable to log its
 * contents, which is very useful for debugging.
 * 
 * Credit: http://www.myjavarecipes.com/how-to-read-post-request-data-twice-in-spring/
 *
 */
public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
	private String _body;

	public MultiReadHttpServletRequest(ServletRequest request) throws IOException {
		super((HttpServletRequest) request);
		
		_body = "";
		
		BufferedReader bufferedReader = request.getReader();
		
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			_body += line;
		}
	}
	
	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(_body.getBytes());
		
		return new ServletInputStream() {
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// Uhh ...
			}
		};
	}
	
	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}
}