package edu.ucdavis.dss.ipa.repositories;

import edu.ucdavis.dss.dw.DwClient;
import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.dw.dto.DwSection;
import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
@Profile({"development", "production", "staging"})
public class RestDataWarehouseRepository implements DataWarehouseRepository {
	/**
	 * Returns a list of people from DW or null on error.
	 *
	 * @param query
	 * @return
     */
	public List<DwPerson> searchPeople(String query) {
		DwClient dwClient = null;

		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());
			
			return dwClient.searchPeople(query);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	/**
	 * Retrieves JSON of terms from DW
	 * @return a list of DwTerms
	 */
	public List<DwTerm> getTerms() {
		DwClient dwClient = null;

		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

			return dwClient.getTerms();
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
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
		DwClient dwClient = null;

		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

			return dwClient.getPersonByLoginId(loginId);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public DwCourse searchCourses(String subjectCode, String courseNumber, String effectiveTermCode) {
		DwClient dwClient = null;
		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

			return dwClient.searchCourses(subjectCode, courseNumber, effectiveTermCode);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public List<DwCourse> queryCourses(String query) {
		DwClient dwClient = null;
		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

			return dwClient.queryCourses(query);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}


	@Override
	public List<DwSection> getSectionsByTermCodeAndUniqueKeys(String termCode, List<String> uniqueKeys) {
		DwClient dwClient = null;
		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

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
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public List<DwSection> getSectionsBySubjectCodeAndYear(String subjectCode, Long year) {
		DwClient dwClient = null;
		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

			List<DwSection> dwSections = dwClient.getDetailedSectionsBySubjectCodeAndYear(subjectCode, year);

			return dwSections;
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}

	@Override
	public List<DwSection> getSectionsBySubjectCodeAndTermCode(String subjectCode, String termCode) {
		DwClient dwClient = null;
		try {
			dwClient = new DwClient(SettingsConfiguration.getDwUrl(), SettingsConfiguration.getDwToken(), SettingsConfiguration.getDwPort());

			List<DwSection> dwSections = dwClient.getDetailedSectionsBySubjectCodeAndTermCode(subjectCode, termCode);

			return dwSections;
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
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
