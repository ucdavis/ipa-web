package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.dw.dto.DwPerson;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.repositories.UserRepository;
import edu.ucdavis.dss.ipa.services.InstructorService;
import edu.ucdavis.dss.ipa.services.RoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import edu.ucdavis.dss.ipa.utilities.Email;

@Service
public class JpaUserService implements UserService {
	private static final Logger log = LogManager.getLogger();

	@Inject UserRepository userRepository;
	@Inject RoleService roleService;
	@Inject InstructorService instructorService;
	@Inject DataWarehouseRepository dwRepository;

	@Override
	@Transactional
	public User saveUser(User user)
	{
		return this.userRepository.save(user);
	}

	public User getUserByLoginId(String loginId)
	{
		return this.userRepository.findByLoginId(loginId);
	}

	@Override
	public User getUserById(Long id) {
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
	public User findOrCreateUserByLoginId(String loginId) {
		User user = this.userRepository.findByLoginId(loginId);

		if(user == null) {
			user = this.createUser(loginId);
			if(user == null) {
				log.error("Could not find " + loginId + " in DW for findOrCreateUserByLoginId()!");
				return null;
			}
		}

		return user;
	}

	@Override
	public User createUser(String loginId) {
		User user = null;
		List<DwPerson> dwPeopleResults = null;

		try {
			dwPeopleResults = dwRepository.searchPeople(loginId);
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
		}

		if(dwPeopleResults != null) {
			for(DwPerson dwPerson : dwPeopleResults) {
				if((dwPerson.getLoginId() != null) && (dwPerson.getLoginId().equalsIgnoreCase(loginId))) {
					user = new User();

					user.setFirstName(dwPerson.getFirst());
					user.setLastName(dwPerson.getLast() );
					user.setLoginId(loginId);
					user.setEmail(dwPerson.getEmail());

					user = this.userRepository.save(user);

					return user;
				}
			}
		}

		return null;
	}

	@Override
	public void contactUser(User user, String messageBody, String subject) {

		String email = user.getEmail();
		if (email == null) {
			log.error("email for user '" + user.getId() + "' is null.");
			return;
		}

		Email.send(email, messageBody, subject);
	}

	@Override
	public List<User> searchAllUsersByFirstLastAndLoginId(String query) {
		return this.userRepository.findByFirstNameOrLastNameOrLoginId(query);
	}
}
