package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.dw.DwClient;
import edu.ucdavis.dss.dw.dto.DwCensus;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.utilities.EmailService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Repository
@Profile({"development", "production", "staging"})
public class RestDataWarehouseRepository implements DataWarehouseRepository {
	@Inject EmailService emailService;

	@Value("${DW_URL}")
	String dwUrl;

	@Value("${DW_TOKEN}")
	String dwToken;

	@Value("${DW_PORT}")
	String dwPort;

	/**
	 * Returns a list of people from DW or null on error.
	 *
	 * @param query
	 * @return
     */
	public List<DwPerson> searchPeople(String query) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);
			
			return dwClient.searchPeople(query);
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	/**
	 * Retrieves JSON of terms from DW
	 * @return a list of DwTerms
	 */
	public List<DwTerm> getTerms() {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			return dwClient.getTerms();
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	/**
	 * Returns a single individual by login ID if they exist.
	 *
	 * @param loginId
	 * @return
	 */
	public DwPerson getPersonByLoginId(String loginId) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			return dwClient.getPersonByLoginId(loginId);
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	@Override
	public DwCourse findCourse(String subjectCode, String courseNumber, String effectiveTermCode) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			return dwClient.findCourse(subjectCode, courseNumber, effectiveTermCode);
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	@Override
	public List<DwCourse> searchCourses(String query) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			return dwClient.searchCourses(query);
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	/**
	 * Returns sections based on the given parameters.
	 *
	 * @param termCode e.g. 201810
	 * @param uniqueKeys e.g. "ECS-010A-001", "ECS-1100-A01"
	 * @return
	 */
	@Override
	public List<DwSection> getSectionsByTermCodeAndUniqueKeys(String termCode, List<String> uniqueKeys) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			List<DwSection> dwSections = new ArrayList<>();

			// Split calls to DW to control the GET param length
			int UNIQUE_KEY_CHUNK_SIZE = 25;
			List<List<String>> uniqueKeysChunks = splitListIntoChunksOfSize(
					uniqueKeys, UNIQUE_KEY_CHUNK_SIZE);

			for (List<String> uniqueKeysChunk: uniqueKeysChunks) {
				dwSections.addAll(
					dwClient.getSectionsByTermCodeAndUniqueKeys(
						termCode, StringUtils.join(uniqueKeysChunk, ","))
				);
			}

			return dwSections;
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	@Override
	public List<DwSection> getSectionsBySubjectCodeAndYear(String subjectCode, Long year) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			List<DwSection> dwSections = dwClient.getDetailedSectionsBySubjectCodeAndYear(subjectCode, year);

			return dwSections;
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	@Override
	public List<DwSection> getSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			List<DwSection> dwSections = dwClient.getDetailedSectionsBySubjectCodeAndTermCode(subjectCode, termCode);

			return dwSections;
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	public List<DwCensus> getCensusBySubjectCodeAndTermCode(String subjectCode, String termCode) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			List<DwCensus> dwCensuses = dwClient.getCensusBySubjectCodeAndTermCode(subjectCode, termCode);

			return dwCensuses;
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	public List<DwCensus> getCensusBySubjectCodeAndCourseNumber(String subjectCode, String courseNumber) {
		DwClient dwClient;

		try {
			dwClient = new DwClient(dwUrl, dwToken, dwPort);

			List<DwCensus> dwCensuses = dwClient.getCensusBySubjectCodeAndCourseNumber(subjectCode, courseNumber);

			return dwCensuses;
		} catch (Exception e) {
			emailService.reportException(e, this.getClass().getName());
			return null;
		}
	}

	private List<List<String>> splitListIntoChunksOfSize(List<String> arr, int chunkSize) {
		List<List<String>> chunks = new ArrayList<>();
		int len = arr.size();

		for (int i = 0; i < len; i += chunkSize) {
			int endIndex = Math.min(i + chunkSize, len);
			chunks.add(arr.subList(i, endIndex));
		}

		return chunks;
	}
}
