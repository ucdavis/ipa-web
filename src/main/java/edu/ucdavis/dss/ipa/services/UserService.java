package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.User;

@Validated
public interface UserService {
	User saveUser(@NotNull @Valid User user);

	User getUserByLoginId(String loginId);
	
	User getUserById(Long id);
	
	List<User> getAllUsers();
	
	List<User> searchAllUsersByFirstLastAndLoginId(String query);

	/**
	 * Find or create user by login ID. Will search DW when creating a new user
	 * to fill in additional details.
	 * 
	 * @param loginId
	 * @return
	 */
	User findOrCreateUserByLoginId(String loginId);

	/**
	 * Creates and saves a user. Uses DW to fill in additional details
	 * if loginId can be matched with DW results.
	 * 
	 * @param loginId
	 * @return
	 */
	User createUser(String loginId);
	
	void contactUser(@Valid User user, String messageBody, String subject);
}