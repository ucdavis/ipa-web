package edu.ucdavis.dss.dw;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwDepartment;
import edu.ucdavis.dss.dw.dto.DwInstructor;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

@Service("DwClientService")
public class DwClient {
	private static final Logger log = LogManager.getLogger();

	private CloseableHttpClient httpclient;
	private HttpHost targetHost;
	private HttpClientContext context;
	private String ApiUrl, ApiToken;
	private int ApiPort;

	public DwClient() throws Exception {
		String filename = System.getProperty("user.home") + File.separator + ".ipa" + File.separator + "dss-dw.properties";
		File propsFile = new File(filename);

		try {
			InputStream input;

			input = new FileInputStream(propsFile);

			Properties prop = new Properties();

			prop.load(input);
			input.close();

			if (prop.getProperty("URL") == null) {
				throw new Exception(filename + " is missing required URL property.");
			}
			if (prop.getProperty("TOKEN") == null) {
				throw new Exception(filename + " is missing required TOKEN property.");
			}
			if (prop.getProperty("PORT") == null) {
				throw new Exception(filename + " is missing required PORT property.");
			}
			
			ApiUrl = prop.getProperty("URL");
			ApiToken = prop.getProperty("TOKEN");
			ApiPort = Integer.parseInt(prop.getProperty("PORT"));
		} catch (FileNotFoundException e) {
			throw new Exception("Could not find " + filename + ".");
		} catch (IOException e) {
			throw new Exception("Unhandled IOException in DwClient: " + e);
		}
	}

	private boolean connect() {
		// Set the default timeout to be 90 seconds.
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(90 * 1000).build();

		httpclient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
		
		targetHost = new HttpHost(ApiUrl, ApiPort, "https");

		CredentialsProvider credsProvider = new BasicCredentialsProvider();
		credsProvider.setCredentials(
				new AuthScope(targetHost.getHostName(), targetHost.getPort()),
				new UsernamePasswordCredentials("nobody", "nothing"));

		AuthCache authCache = new BasicAuthCache();
		BasicScheme basicAuth = new BasicScheme();
		
		authCache.put(targetHost, basicAuth);

		// Add AuthCache to the execution context
		context = HttpClientContext.create();
		context.setCredentialsProvider(credsProvider);
		context.setAuthCache(authCache);

		return true;
	}

	public DwCourse getCourseBySubjectCodeAndCourseNumberAndEffectiveTermAndTermCode(
			String subjectCode, String courseNumber, String effectiveTermCode, String termCode) {
		if (connect() && !subjectCode.equals("") && !courseNumber.equals("") && !effectiveTermCode.equals("")) {
			DwCourse course = new DwCourse();

			try {
				HttpGet httpget = new HttpGet("/courses/details?courseNumber=" + URLEncoder.encode(courseNumber, "UTF-8") + "&subjectCode="
						+ URLEncoder.encode(subjectCode, "UTF-8") + "&effectiveTerm=" + URLEncoder.encode(effectiveTermCode, "UTF-8")
						+ "&termCode=" + URLEncoder.encode(termCode, "UTF-8")+ "&token=" + ApiToken);
				
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
				
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				course = mapper.readValue(
						EntityUtils.toString(entity),
						mapper.getTypeFactory().constructType(DwCourse.class));

				response.close();

			} catch (IOException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
			return course;
		} else {
			log.info("Course could not be queried due to insufficient information provided");
		}
		
		return null;
	}

	/**
	 * Fetches all section groups by department code and year, including do not print CRNs.
	 * 
	 * @param deptCode
	 * @param year
	 * @return
	 */
	public Set<DwSectionGroup> getSectionGroupsByDeptCodeAndYear(String deptCode, long year) {
		return getSectionGroupsByDeptCodeAndYear(deptCode, year, true, true);
	}

	public Set<DwSectionGroup> getPrivateSectionGroupsByDeptCodeAndYear(String deptCode, long year) {
		return getSectionGroupsByDeptCodeAndYear(deptCode, year, false, true);
	}

	public Set<DwSectionGroup> getSectionGroupsByDeptCodeAndYear(String deptCode, long year, boolean includePublic, boolean includePrivate) {
		if (connect()) {
			log.debug("Beginning HTTP GET from DW for sectionGroups/details ...");
			Set<DwSectionGroup> dwSectionGroups = null;
			String url = null;
			
			if(includePublic && !includePrivate) {
				// Only public
				url = "/sectionGroups/details?showPrivate=false";
			} else if(!includePublic && includePrivate) {
				// Only private
				url = "/sectionGroups/details?onlyPrivate=true";
			} else if(!includePublic && !includePrivate) {
				// Nothing. Sounds good.
				return null;
			} else {
				// Both public and private
				url = "/sectionGroups/details?showPrivate=true";
			}

			try {
				HttpGet httpget = new HttpGet(url + "&deptCodes=" + URLEncoder.encode(deptCode, "UTF-8") + "&year=" + URLEncoder.encode(Long.toString(year), "UTF-8") + "&token=" + ApiToken);

				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
				
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}
				
				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					dwSectionGroups = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									Set.class, DwSectionGroup.class));
				} else {
					log.warn("getSectionGroupsByDeptNumberAndYear Response from DW returned null for deptCode = " + deptCode + " and year = " + year);
				}
				
				log.debug("Finished sectionGroups/details parsing for DW.");

				response.close();

			} catch (IOException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}

			return dwSectionGroups;
		}

		return null;
	}

	/**
	 * Returns a list of all SIS (Banner) departments. Does not include PPS departments.
	 * 
	 * @return
	 */
	public List<DwDepartment> getAllSisDepartments() {
		if (connect()) {
			List<DwDepartment> departments = null;

			try {
				HttpGet httpget = new HttpGet("/departments/sis?token=" + ApiToken);

				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
			
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}
				
				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					departments = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwDepartment.class));
				} else {
					log.warn("getAllDepartments Response from DW returned null");
				}
				log.debug("Finished /departments/sis parsing.");

				response.close();

			} catch (IOException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}

			return departments;
		} else {
			log.info("Could not connect to Data Warehouse");
		}
		return null;
	}

	/**
	 * Searches all people based on 'query'. May match against multiple fields.
	 * 
	 * @param query
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<DwPerson> searchPeople(String query) throws ClientProtocolException, IOException {
		List<DwPerson> dwPeople = null;
		
		if (connect() && query != null) {
			HttpGet httpget = new HttpGet("/people.json?q=" + URLEncoder.encode(query, "UTF-8") + "&token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
				
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();
				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					dwPeople = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwPerson.class));
				} else {
					log.warn("searchUsers Response from DW returned null, for criterion = " + query);
				}

				response.close();
			} catch (JsonParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else if (query == null) {
			log.info("No query text.");
		}
		
		return dwPeople;
	}

	/**
	 * Searches all instructors based on 'query'. Matches against first or last name.
	 *
	 * @param query
	 * @return
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<DwInstructor> searchInstructors(String query) throws ClientProtocolException, IOException {
		List<DwInstructor> dwInstructors = null;

		if (connect() && query != null) {
			HttpGet httpget = new HttpGet("/instructors/search?q=" + URLEncoder.encode(query, "UTF-8") + "&token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);

				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();
				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					dwInstructors = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwInstructor.class));
				} else {
					log.warn("searchInstructors Response from DW returned null, for criterion = " + query);
				}

				response.close();
			} catch (JsonParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else if (query == null) {
			log.info("No query text.");
		}

		return dwInstructors;
	}

	public List<DwInstructor> getInstructorsByDeptCode(String departmentCode) throws ClientProtocolException, IOException {
		List<DwInstructor> dwInstructors = null;

		if (connect() && departmentCode != null) {
			HttpGet httpget = new HttpGet("/departments/sis/" + URLEncoder.encode(departmentCode, "UTF-8") + "/instructors?token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
				
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					dwInstructors = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwInstructor.class));
				} else {
					log.warn("getDepartmentUsersByDeptCode received unexpected DW response.");
				}

				response.close();

			} catch (JsonParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else if (departmentCode == null) {
			log.info("No department code given.");
		}
		
		return dwInstructors;
	}

	public List<DwTerm> getAllTerms() throws ClientProtocolException, IOException {
		List<DwTerm> dwTerms = null;

		if (connect()) {
			HttpGet httpget = new HttpGet("/terms?token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);
				
				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					dwTerms = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwTerm.class));
				} else {
					log.warn("getAllTerms received unexpected DW response.");
				}

				response.close();

			} catch (JsonParseException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		}
		
		return dwTerms;
	}

	/**
	 * Searches all courses based on 'query'. Matches against title, subjectCode, or courseNumber.
	 *
	 * @param query String with the search query
	 * @return courses List of matching courses
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<DwCourse> searchCourses(String query) throws ClientProtocolException, IOException {
		List<DwCourse> courses = new ArrayList<DwCourse>();
		if (connect() && query != null && query.length() > 2) {
			HttpGet httpget = new HttpGet("/courses/search?q=" + URLEncoder.encode(query, "UTF-8") + "&token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);

				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
				if (arrNode != null) {
					courses = mapper.readValue(
							arrNode.toString(),
							mapper.getTypeFactory().constructCollectionType(
									List.class, DwCourse.class));
				} else {
					log.warn("searchCourses Response from DW returned null, for criterion = " + query);
				}

				response.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return courses;
	}

	/**
	 * Returns all courses based on 'query'. Matches against title, subjectCode, or courseNumber.
	 *
	 * @param subjectCode String with course subject code (e.g. PSC)
	 * @param courseNumber String with course number (e.g. 001)
	 * @param effectiveTermCode String with effective term code (e.g. 197910)
	 * @param termCode String with the latest term to include (e.g. 201510)
	 * @return DwSectionGroups List of dwSectionGroups matching the term for 5 years
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	public List<DwSectionGroup> getCourseCensusBySubjectCodeAndCourseNumberAndEffectiveTermAndTermCode(
			String subjectCode, String courseNumber, String effectiveTermCode, String termCode) {
		if (connect() && !subjectCode.equals("") && !courseNumber.equals("") && !effectiveTermCode.equals("")) {
			List<DwSectionGroup> sectionGroups = new ArrayList<DwSectionGroup>();

			try {
				HttpGet httpget = new HttpGet("/courses/census?courseNumber=" + URLEncoder.encode(courseNumber, "UTF-8") + "&subjectCode="
						+ URLEncoder.encode(subjectCode, "UTF-8") + "&effectiveTerm=" + URLEncoder.encode(effectiveTermCode, "UTF-8")
						+ "&termCode=" + URLEncoder.encode(termCode, "UTF-8")+ "&token=" + ApiToken);

				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);

				StatusLine line = response.getStatusLine();
				if(line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). Check URL/parameters.");
				}

				HttpEntity entity = response.getEntity();

				ObjectMapper mapper = new ObjectMapper();

				sectionGroups = mapper.readValue(
						EntityUtils.toString(entity),
						mapper.getTypeFactory().constructCollectionType(
								List.class, DwSectionGroup.class));

				response.close();

			} catch (IOException e) {
				ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
			return sectionGroups;
		} else {
			log.info("Course Census could not be queried due to insufficient information provided");
		}

		return null;
	}

}
