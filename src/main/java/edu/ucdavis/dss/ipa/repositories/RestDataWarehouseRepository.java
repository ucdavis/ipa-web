package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import edu.ucdavis.dss.dw.dto.DwCourse;
import edu.ucdavis.dss.dw.dto.DwTerm;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import edu.ucdavis.dss.dw.DwClient;
import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;

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
}
