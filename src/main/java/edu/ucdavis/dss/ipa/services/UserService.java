package edu.ucdavis.dss.ipa.services;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import edu.ucdavis.dss.ipa.entities.User;

@Validated
public interface UserService {

	User save(@NotNull @Valid User user);

	User getOneByLoginId(String loginId);
	
	User getOneById(Long id);
	
	List<User> getAllUsers();
	
	List<User> searchByFirstLastAndLoginId(String query);

	/**
	 * Find or create user by login ID. Will search DW when creating a new user
	 * to fill in additional details.
	 * 
	 * @param loginId
	 * @return
	 */
	User findOrCreateByLoginId(String loginId);

	/**
	 * Creates and saves a user. Uses DW to fill in additional details
	 * if loginId can be matched with DW results.
	 * 
	 * @param loginId
	 * @return
	 */
	User createByLoginId(String loginId);
	
	void contact(@Valid User user, String messageBody, String subject);

    void updateLastAccessed(User user);
}