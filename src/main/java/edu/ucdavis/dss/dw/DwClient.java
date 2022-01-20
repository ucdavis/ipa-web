package edu.ucdavis.dss.dw;

import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.dw.dto.DwSearchResultSection;
import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.dw.dto.DwTerm;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.ucdavis.dss.dw.dto.DwPerson;

import javax.inject.Inject;

public class DwClient {
	private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.dss.dw.DwClient");

	private CloseableHttpClient httpclient;
	private HttpHost targetHost;
	private HttpClientContext context;
	private String ApiUrl, ApiToken;
	private int ApiPort;

	public DwClient(String url, String token, String port) throws Exception {
		ApiUrl = url;
		ApiToken = token;
		ApiPort = Integer.parseInt(port);
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
			HttpGet httpget = new HttpGet("/people/search?q=" + URLEncoder.encode(query, "UTF-8") + "&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(
					targetHost, httpget, context);

			StatusLine line = response.getStatusLine();
			if(line.getStatusCode() != HttpStatus.OK.value()) {
				throw new IllegalStateException("Data Warehouse did not return 200 OK (was " + line.getStatusCode() + "). URL: /people/search?q=" + URLEncoder.encode(query, "UTF-8"));
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
		} else if (query == null) {
			log.warn("No query given.");
		}

		return dwPeople;
	}

	public List<DwTerm> getTerms() throws IOException {
		List<DwTerm> dwTerms = null;

		if(connect()) {
			// https://beta.dw.dss.ucdavis.edu/terms?token=dssit
			HttpGet httpGet = new HttpGet("/terms?token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(targetHost, httpGet, context);
			StatusLine line = response.getStatusLine();

			if(line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /terms");
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));

			if (arrNode != null) {
				dwTerms = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(List.class, DwTerm.class));
			} else {
				log.warn("getTerms Reponse from DW returned null");
			}

			response.close();
		} else {
			log.warn("Could not connect to DW");
		}

		return dwTerms;
	}

	public DwPerson getPersonByLoginId(String loginId) throws UnsupportedEncodingException {
		DwPerson dwPerson = null;

		if (connect() && loginId != null) {
			HttpGet httpget = new HttpGet("/people/" + URLEncoder.encode(loginId, "UTF-8") + "?token=" + ApiToken);

			try {
				CloseableHttpResponse response = httpclient.execute(
						targetHost, httpget, context);

				StatusLine line = response.getStatusLine();
				if (line.getStatusCode() == 404) {
					// DW has no such user
					return null;
				}
				if (line.getStatusCode() != 200) {
					throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /people/" + URLEncoder.encode(loginId, "UTF-8"));
				}

				HttpEntity entity = response.getEntity();

				String entityString = EntityUtils.toString(entity);

				if ((entityString != null) && (entityString.length() > 0)) {
					dwPerson = new DwPerson();

					JsonNode node = new ObjectMapper().readTree(entityString);

					JsonNode contactInfo = node.get("contactInfo");
					JsonNode person = node.get("person");
					JsonNode prikerbacct = node.get("prikerbacct");

					if(contactInfo != null) {
						if(contactInfo.get("iamId") != null) {
							dwPerson.setIamId(contactInfo.get("iamId").asText());
						}
						if(contactInfo.get("email") != null) {
							dwPerson.setEmail(contactInfo.get("email").textValue());
						}
					}

					if(person != null) {
						dwPerson.setdFirstName(person.get("dFirstName").textValue());
						dwPerson.setdFullName(person.get("dFullName").textValue());
						dwPerson.setdLastName(person.get("dLastName").textValue());
						dwPerson.setdMiddleName(person.get("dMiddleName").textValue());
						dwPerson.setoFirstName(person.get("oFirstName").textValue());
						dwPerson.setoFullName(person.get("oFullName").textValue());
						dwPerson.setoLastName(person.get("oLastName").textValue());
						dwPerson.setoMiddleName(person.get("oMiddleName").textValue());
					}

					if(prikerbacct != null) {
						dwPerson.setUserId(prikerbacct.get("userId").textValue());
					}
				}

				response.close();
			} catch (IOException e) {
				//ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else if (loginId == null) {
			log.warn("No login ID given.");
		}

		return dwPerson;
	}

	public List<DwCourse> searchCourses(String query) throws IOException {
		List<DwCourse> dwCourses = null;

		if (connect() && query != null) {
			HttpGet httpget = new HttpGet("/courses/search?q=" + URLEncoder.encode(query, "UTF-8") + "&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(
					targetHost, httpget, context);

			StatusLine line = response.getStatusLine();
			if(line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /courses/search?q=" + URLEncoder.encode(query, "UTF-8"));
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
			if (arrNode != null) {
				dwCourses = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(
								List.class, DwCourse.class));
			} else {
				log.warn("searchUsers Response from DW returned null, for criterion = " + query);
			}

			response.close();
		} else if (query == null) {
			log.warn("No query given.");
		}

		return dwCourses;
	}

	public List<DwSection> getSectionsByTermCodeAndUniqueKeys(String termCode, String sectionUniqueKeys) throws IOException {
		List<DwSection> dwSections = new ArrayList<>();

		if (connect() && termCode != null && sectionUniqueKeys != null) {
			HttpGet httpget = new HttpGet("/sections/details?termCode=" + URLEncoder.encode(termCode, "UTF-8") +
					"&sections=" + URLEncoder.encode(sectionUniqueKeys, "UTF-8") +
					"&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(
					targetHost, httpget, context);

			StatusLine line = response.getStatusLine();
			if (line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /sections/details?termCode=" + URLEncoder.encode(termCode, "UTF-8") +
				"&sections=" + URLEncoder.encode(sectionUniqueKeys, "UTF-8"));
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
			if ((arrNode != null) && (arrNode.get(0) != null)) {
				dwSections = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(
								List.class, DwSection.class));
			}

			response.close();
		} else if (termCode == null) {
			log.warn("No termCode given.");
		} else if (sectionUniqueKeys == null) {
			log.warn("No sectionUniqueKeys given.");
		}

		return dwSections;
	}

	public List<DwSection> getDetailedSectionsBySubjectCodeAndYear(String subjectCode, Long year) throws IOException {
		List<DwSection> dwSections = new ArrayList<>();

		if (connect() && subjectCode != null && year != null) {
			HttpGet httpget = new HttpGet("/sections/details?subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8") +
					"&year=" + URLEncoder.encode(String.valueOf(year), "UTF-8") +
					"&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(
					targetHost, httpget, context);

			StatusLine line = response.getStatusLine();
			if (line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /sections/details?subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8") +
				"&year=" + URLEncoder.encode(String.valueOf(year), "UTF-8"));
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
			if ((arrNode != null) && (arrNode.get(0) != null)) {
				dwSections = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(
								List.class, DwSection.class));
			} else {
				log.warn("getDetailedSectionsBySubjectCodeAndYear: Response from DW returned null, for criterion = " + subjectCode + ", " + year);
			}

			response.close();
		} else if (year == null) {
			log.warn("No year given.");
		} else if (subjectCode == null) {
			log.warn("No subjectCode given.");
		}

		return dwSections;
	}

	public List<DwSection> getDetailedSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode) throws IOException {
		List<DwSection> dwSections = new ArrayList<>();

		if (connect() && subjectCode != null && termCode != null) {
			HttpGet httpget = new HttpGet("/sections/details?subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8") +
					"&termCode=" + URLEncoder.encode(String.valueOf(termCode), "UTF-8") +
					"&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(
					targetHost, httpget, context);

			StatusLine line = response.getStatusLine();
			if (line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /sections/details?subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8") +
				"&termCode=" + URLEncoder.encode(String.valueOf(termCode), "UTF-8"));
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));

			// It is allowable for DW to return null if the subject code is incorrect or
			// the termCode refers to a future term not yet in Banner.
			if ((arrNode != null) && (arrNode.get(0) != null)) {
				dwSections = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(
								List.class, DwSection.class));
			}

			response.close();
		} else if (termCode == null) {
			log.warn("No termCode given.");
		} else if (subjectCode == null) {
			log.warn("No subjectCode given.");
		}

		return dwSections;
	}

	/**
	 * Finds a course in DW with the given subjectCode, courseNumber, and effectiveTermCode
	 *
	 * @param subjectCode e.g. ECS, PHY
	 * @param courseNumber e.g. 101, 10A, 010
	 * @param effectiveTermCode e.g. 200410
	 * @return a DwCourse representing the found course or null if no course found
	 * @throws IOException - if DW returns anything besides 200 or 404
	 */
	public DwCourse findCourse(String subjectCode, String courseNumber, String effectiveTermCode) throws IOException {
		if((subjectCode == null) || (courseNumber == null) || (effectiveTermCode == null)) {
			log.warn("Cannot get course: subjectCode, courseNumber, and/or effectiveTermCode is null.");
			return null;
		}

		if(connect() == false) {
			log.warn("Could not connect to DW while getting course.");
			return null;
		}

		HttpGet httpget = new HttpGet("/courses/details?subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8") +
				"&courseNumber=" + URLEncoder.encode(String.valueOf(courseNumber), "UTF-8") +
				"&effectiveTermCode=" + URLEncoder.encode(String.valueOf(effectiveTermCode), "UTF-8") +
				"&token=" + ApiToken);

		CloseableHttpResponse response = httpclient.execute(
				targetHost, httpget, context);

		StatusLine line = response.getStatusLine();
		if ((line.getStatusCode() != 200) && (line.getStatusCode() != 404)) {
			throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /courses/details?subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8") +
							"&courseNumber=" + URLEncoder.encode(String.valueOf(courseNumber), "UTF-8") + "&effectiveTermCode=" + URLEncoder.encode(String.valueOf(effectiveTermCode), "UTF-8"));
		}

		HttpEntity entity = response.getEntity();
		String entityString = EntityUtils.toString(entity);

		DwCourse course = null;

		if ((entityString != null) && (entityString.length() > 0)) {
			course = new DwCourse();

			JsonNode node = new ObjectMapper().readTree(entityString);

			course.setCourseNumber(node.get("courseNumber").textValue());
			course.setSubjectCode(node.get("subjectCode").textValue());
			course.setEffectiveTermCode(node.get("effectiveTermCode").textValue());
			course.setTitle(node.get("title").textValue());

			if(node.get("unitsLow").isNull() == false) {
				course.setCreditHoursLow(node.get("unitsLow").floatValue());
			}
			if(node.get("unitsHigh").isNull() == false) {
				course.setCreditHoursHigh(node.get("unitsHigh").floatValue());
			}
		}

		return course;
	}

	public List<DwCensus> getCensusBySubjectCodeAndTermCode(String subjectCode, String termCode) throws IOException {
		List<DwCensus> dwCensuses = null;

		if(connect()) {
			HttpGet httpGet = new HttpGet("/census?subjectCode=" + subjectCode + "&termCode=" + termCode + "&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(targetHost, httpGet, context);
			StatusLine line = response.getStatusLine();

			if(line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /terms");
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));

			if (arrNode != null) {
				dwCensuses = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(List.class, DwCensus.class));
			} else {
				log.warn("getCensus response from DW returned null");
			}

			response.close();
		} else {
			log.warn("Could not connect to DW");
		}

		return dwCensuses;
	}

	public List<DwSearchResultSection> searchSections(String subjectCode, Long academicYear) throws IOException {
		List<DwSearchResultSection> dwSearchResultSections = null;

		if (connect() && subjectCode != null) {
			HttpGet httpget = new HttpGet("/sections/search?"
				+ "&subjectCode=" + URLEncoder.encode(subjectCode, "UTF-8")
				+ "&academicYear=" + academicYear
				+ "&private=true"
				+ "&token=" + ApiToken);

			CloseableHttpResponse response = httpclient.execute(
					targetHost, httpget, context);

			StatusLine line = response.getStatusLine();
			if(line.getStatusCode() != 200) {
				throw new IllegalStateException("Data Warehouse did not return a 200 OK (was " + line.getStatusCode() + "). URL: /sections/search?q=" + URLEncoder.encode(subjectCode, "UTF-8"));
			}

			HttpEntity entity = response.getEntity();

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(EntityUtils.toString(entity));
			if (arrNode != null) {
				dwSearchResultSections = mapper.readValue(
						arrNode.toString(),
						mapper.getTypeFactory().constructCollectionType(
								List.class, DwSearchResultSection.class));
			} else {
				log.warn("searchSections Response from DW returned null, for criterion = " + subjectCode);
			}

			response.close();
		} else if (subjectCode == null) {
			log.warn("No query given.");
		}

		return dwSearchResultSections;
	}
}
