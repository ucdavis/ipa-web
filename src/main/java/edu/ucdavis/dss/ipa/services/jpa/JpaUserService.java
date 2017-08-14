package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.api.helpers.Utilities;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.UserRepository;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.utilities.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JpaUserService implements UserService {
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject UserRepository userRepository;
	@Inject DataWarehouseRepository dwRepository;

	@Override
	public User save(User user)
	{
		return this.userRepository.save(user);
	}

	@Override
	public User getOneByLoginId(String loginId)
	{
		return this.userRepository.findByLoginId(loginId);
	}

	@Override
	public User getOneById(Long id) {
		return this.userRepository.findById(id);
	}

	private <E> List<E> toList(Iterable<E> i)
	{
		List<E> list = new ArrayList<>();
		i.forEach(list::add);

		return list;
	}

	@Override
	public List<User> getAllUsers() {
		return this.toList(this.userRepository.findAll());
	}

	@Override
	public User findOrCreateByLoginId(String loginId) {
		User user = this.userRepository.findByLoginId(loginId);

		if(user == null) {
			user = this.createByLoginId(loginId);
			if(user == null) {
				log.error("Could not find " + loginId + " in DW for findOrCreateByLoginId()!");
				return null;
			}
		}

		return user;
	}

	@Override
	public User createByLoginId(String loginId) {
		User user = null;
		DwPerson dwPerson = null;

		try {
			dwPerson = dwRepository.getPersonByLoginId(loginId);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}

		if((dwPerson != null) && (dwPerson.getUserId() != null) && (dwPerson.getUserId().equalsIgnoreCase(loginId))) {
			user = new User();
			user.setLoginId(loginId);
			user.setEmail(dwPerson.getEmail());

			if (dwPerson.getdFirstName() != null && dwPerson.getdFirstName().length() > 0) {
				user.setFirstName(dwPerson.getdFirstName());
			} else {
				// oFirstName is always full caps, ex: 'SMITH'
				String firstName = Utilities.titleize(dwPerson.getoFirstName());
				user.setFirstName(firstName);
			}

			if (dwPerson.getdLastName() != null && dwPerson.getdLastName().length() > 0) {
				user.setLastName(dwPerson.getdLastName());
			} else {
				// oLastName is always full caps, ex: 'SMITH'
				String lastName = Utilities.titleize(dwPerson.getoLastName());
				user.setLastName(lastName);
			}

			user = this.userRepository.save(user);

			return user;
		}

		return null;
	}

	@Override
	public void contact(User user, String messageBody, String subject) {
		String email = user.getEmail();

		if (email == null) {
			// TODO: Report this as an exception maybe?
			log.error("email for user '" + user.getId() + "' is null.");
			return;
		}

		Email.send(email, messageBody, subject);
	}

	@Override
	public void updateLastAccessed(User user) {
		if (user == null) { return; }
		user.setLastAccessed(new Date());
		this.save(user);
	}

	@Override
	public List<User> searchByFirstLastAndLoginId(String query) {
		return this.userRepository.findByFirstNameOrLastNameOrLoginId(query);
	}
}
