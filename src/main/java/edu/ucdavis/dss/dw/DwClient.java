package edu.ucdavis.dss.dw;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.dw.dto.DwTerm;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DwClient {
	private static final Logger log = LoggerFactory.getLogger("edu.ucdavis.dss.dw.DwClient");
	private final String baseUrl, apiToken;
	private final CloseableHttpClient httpClient;

	public DwClient(String apiUrl, String apiToken) {
		this.baseUrl = "https://" + apiUrl;
		this.apiToken = apiToken;

		RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(Timeout.ofSeconds(90)).build();
		HttpRequestRetryStrategy retryStrategy = new DefaultHttpRequestRetryStrategy(3, TimeValue.ofMilliseconds(100));

		this.httpClient =
			HttpClients.custom().setDefaultRequestConfig(requestConfig).setRetryStrategy(retryStrategy).build();
	}

	/**
	 * Searches all people based on 'query'. May match against multiple fields.
	 *
	 * @param query
	 * @return
	 * @throws IOException
	 */
	public List<DwPerson> searchPeople(String query) throws IOException {
		List<DwPerson> dwPeople = null;

		if (query != null) {
			HttpGet httpGet = new HttpGet(
				this.baseUrl + "/people/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&token=" +
					apiToken);

			String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));

			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(entityString);
			if (arrNode != null) {
				dwPeople = mapper.readValue(arrNode.toString(),
					mapper.getTypeFactory().constructCollectionType(List.class, DwPerson.class));
			} else {
				log.warn("searchUsers Response from DW returned null, for criterion = " + query);
			}
		} else {
			log.warn("No query given.");
		}

		return dwPeople;
	}

	public List<DwTerm> getTerms() throws IOException {
		List<DwTerm> dwTerms = null;

		HttpGet httpGet = new HttpGet(this.baseUrl + "/terms?token=" + apiToken);

		String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode arrNode = new ObjectMapper().readTree(entityString);

		if (arrNode != null) {
			dwTerms = mapper.readValue(arrNode.toString(),
				mapper.getTypeFactory().constructCollectionType(List.class, DwTerm.class));
		} else {
			log.warn("getTerms Reponse from DW returned null");
		}
		return dwTerms;
	}

	public DwPerson getPersonByLoginId(String loginId) throws UnsupportedEncodingException {
		DwPerson dwPerson = null;

		if (loginId != null) {
			HttpGet httpGet = new HttpGet(
				this.baseUrl + "/people/" + URLEncoder.encode(loginId, StandardCharsets.UTF_8) + "?token=" + apiToken);

			try {
				String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));

				if ((entityString != null) && (entityString.length() > 0)) {
					dwPerson = new DwPerson();

					JsonNode node = new ObjectMapper().readTree(entityString);

					JsonNode contactInfo = node.get("contactInfo");
					JsonNode person = node.get("person");
					JsonNode prikerbacct = node.get("prikerbacct");

					if (contactInfo != null) {
						if (contactInfo.get("iamId") != null) {
							dwPerson.setIamId(contactInfo.get("iamId").asText());
						}
						if (contactInfo.get("email") != null) {
							dwPerson.setEmail(contactInfo.get("email").textValue());
						}
					}

					if (person != null) {
						dwPerson.setdFirstName(person.get("dFirstName").textValue());
						dwPerson.setdFullName(person.get("dFullName").textValue());
						dwPerson.setdLastName(person.get("dLastName").textValue());
						dwPerson.setdMiddleName(person.get("dMiddleName").textValue());
						dwPerson.setoFirstName(person.get("oFirstName").textValue());
						dwPerson.setoFullName(person.get("oFullName").textValue());
						dwPerson.setoLastName(person.get("oLastName").textValue());
						dwPerson.setoMiddleName(person.get("oMiddleName").textValue());
					}

					if (prikerbacct != null) {
						dwPerson.setUserId(prikerbacct.get("userId").textValue());
					}
				}

			} catch (IOException e) {
				//ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			}
		} else {
			log.warn("No login ID given.");
		}

		return dwPerson;
	}

	public List<DwCourse> searchCourses(String query) throws IOException {
		List<DwCourse> dwCourses = null;

		if (query != null) {
			HttpGet httpGet = new HttpGet(
				this.baseUrl + "/courses/search?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&token=" +
					apiToken);

			String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(entityString);
			if (arrNode != null) {
				dwCourses = mapper.readValue(arrNode.toString(),
					mapper.getTypeFactory().constructCollectionType(List.class, DwCourse.class));
			} else {
				log.warn("searchUsers Response from DW returned null, for criterion = " + query);
			}

			return dwCourses;
		} else {
			log.warn("No query given.");
		}

		return dwCourses;
	}

	public List<DwSection> getSectionsByTermCodeAndUniqueKeys(String termCode, String sectionUniqueKeys)
		throws IOException {
		List<DwSection> dwSections = new ArrayList<>();

		if (termCode != null && sectionUniqueKeys != null) {
			HttpGet httpGet = new HttpGet(
				this.baseUrl + "/sections/details?termCode=" + URLEncoder.encode(termCode, StandardCharsets.UTF_8) +
					"&sections=" + URLEncoder.encode(sectionUniqueKeys, StandardCharsets.UTF_8) + "&token=" + apiToken);

			String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(entityString);
			if ((arrNode != null) && (arrNode.get(0) != null)) {
				dwSections = mapper.readValue(arrNode.toString(),
					mapper.getTypeFactory().constructCollectionType(List.class, DwSection.class));
			}
		} else if (termCode == null) {
			log.warn("No termCode given.");
		} else if (sectionUniqueKeys == null) {
			log.warn("No sectionUniqueKeys given.");
		}

		return dwSections;
	}

	public List<DwSection> getDetailedSectionsBySubjectCodeAndYear(String subjectCode, Long year) throws IOException {
		List<DwSection> dwSections = new ArrayList<>();

		if (subjectCode != null && year != null) {
			HttpGet httpGet = new HttpGet(this.baseUrl + "/sections/details?subjectCode=" +
				URLEncoder.encode(subjectCode, StandardCharsets.UTF_8) + "&year=" +
				URLEncoder.encode(String.valueOf(year), StandardCharsets.UTF_8) + "&token=" + apiToken);

			String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(entityString);
			if ((arrNode != null) && (arrNode.get(0) != null)) {
				dwSections = mapper.readValue(arrNode.toString(),
					mapper.getTypeFactory().constructCollectionType(List.class, DwSection.class));
			} else {
				log.warn("getDetailedSectionsBySubjectCodeAndYear: Response from DW returned null, for criterion = " +
					subjectCode + ", " + year);
			}

		} else if (year == null) {
			log.warn("No year given.");
		} else if (subjectCode == null) {
			log.warn("No subjectCode given.");
		}

		return dwSections;
	}

	public List<DwSection> getDetailedSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode)
		throws IOException {
		List<DwSection> dwSections = new ArrayList<>();

		if (subjectCode != null && termCode != null) {
			HttpGet httpGet = new HttpGet(this.baseUrl + "/sections/details?subjectCode=" +
				URLEncoder.encode(subjectCode, StandardCharsets.UTF_8) + "&termCode=" +
				URLEncoder.encode(String.valueOf(termCode), StandardCharsets.UTF_8) + "&token=" + apiToken);

			String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));
			ObjectMapper mapper = new ObjectMapper();
			JsonNode arrNode = new ObjectMapper().readTree(entityString);
			if ((arrNode != null) && (arrNode.get(0) != null)) {
				dwSections = mapper.readValue(arrNode.toString(),
					mapper.getTypeFactory().constructCollectionType(List.class, DwSection.class));
			}
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
	 * @param subjectCode       e.g. ECS, PHY
	 * @param courseNumber      e.g. 101, 10A, 010
	 * @param effectiveTermCode e.g. 200410
	 * @return a DwCourse representing the found course or null if no course found
	 * @throws IOException - if DW returns anything besides 200 or 404
	 */
	public DwCourse findCourse(String subjectCode, String courseNumber, String effectiveTermCode) throws IOException {
		if ((subjectCode == null) || (courseNumber == null) || (effectiveTermCode == null)) {
			log.warn("Cannot get course: subjectCode, courseNumber, and/or effectiveTermCode is null.");
			return null;
		}

		HttpGet httpGet = new HttpGet(
			this.baseUrl + "/courses/details?subjectCode=" + URLEncoder.encode(subjectCode, StandardCharsets.UTF_8) +
				"&courseNumber=" + URLEncoder.encode(courseNumber, StandardCharsets.UTF_8) + "&effectiveTermCode=" +
				URLEncoder.encode(effectiveTermCode, StandardCharsets.UTF_8) + "&token=" + apiToken);

		String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));

		DwCourse course = null;

		if ((entityString != null) && (entityString.length() > 0)) {
			course = new DwCourse();

			JsonNode node = new ObjectMapper().readTree(entityString);

			course.setCourseNumber(node.get("courseNumber").textValue());
			course.setSubjectCode(node.get("subjectCode").textValue());
			course.setEffectiveTermCode(node.get("effectiveTermCode").textValue());
			course.setTitle(node.get("title").textValue());

			if (node.get("unitsLow").isNull() == false) {
				course.setCreditHoursLow(node.get("unitsLow").floatValue());
			}
			if (node.get("unitsHigh").isNull() == false) {
				course.setCreditHoursHigh(node.get("unitsHigh").floatValue());
			}
		}

		return course;
	}

	public List<DwCensus> getCensusBySubjectCodeAndTermCode(String subjectCode, String termCode) throws IOException {
		List<DwCensus> dwCensuses = null;

		HttpGet httpGet = new HttpGet(
			this.baseUrl + "/census?subjectCode=" + subjectCode + "&termCode=" + termCode + "&token=" + apiToken);

		String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));

		ObjectMapper mapper = new ObjectMapper();
		JsonNode arrNode = new ObjectMapper().readTree(entityString);

		if (arrNode != null) {
			dwCensuses = mapper.readValue(arrNode.toString(),
				mapper.getTypeFactory().constructCollectionType(List.class, DwCensus.class));
		} else {
			log.warn("getCensus response from DW returned null");
		}

		return dwCensuses;
	}

	public List<DwCensus> getCensusBySubjectCodeAndCourseNumber(String subjectCode, String courseNumber)
		throws IOException {
		List<DwCensus> dwCensuses = null;

		HttpGet httpGet = new HttpGet(
			this.baseUrl + "/census?subjectCode=" + subjectCode + "&courseNumber=" + courseNumber + "&token=" +
				apiToken);

		String entityString = httpClient.execute(httpGet, new DwResponseHandler(httpGet.getPath()));
		ObjectMapper mapper = new ObjectMapper();
		JsonNode arrNode = new ObjectMapper().readTree(entityString);

		if (arrNode != null) {
			dwCensuses = mapper.readValue(arrNode.toString(),
				mapper.getTypeFactory().constructCollectionType(List.class, DwCensus.class));
		} else {
			log.warn("getCensus response from DW returned null");
		}

		return dwCensuses;
	}

	private record DwResponseHandler(String url) implements HttpClientResponseHandler<String> {

		@Override
		public String handleResponse(ClassicHttpResponse response) throws IOException, ParseException {
			int statusCode = response.getCode();

			if ((statusCode >= 200 && statusCode < 300) || statusCode == 404) {
				return EntityUtils.toString(response.getEntity());
			} else {
				String strippedUrl = url.substring(0, url.indexOf("token=") - 1);
				log.error("Request to URL: {} failed with status code: {}", strippedUrl, statusCode);
				return null;
			}
		}
	}
}
