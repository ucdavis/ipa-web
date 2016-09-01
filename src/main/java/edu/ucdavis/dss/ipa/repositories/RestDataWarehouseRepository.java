package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

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
			dwClient = new DwClient();
			
			return dwClient.searchPeople(query);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}
	}
}
