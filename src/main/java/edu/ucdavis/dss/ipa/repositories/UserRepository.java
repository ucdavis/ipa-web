package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.ucdavis.dss.ipa.entities.User;

public interface UserRepository extends CrudRepository<User, Long> {
	User findByLoginId(String loginId);
	
	User findByToken(String token);

	@Query("SELECT u FROM User u WHERE u.loginId LIKE :query% OR u.firstName LIKE :query% OR u.lastName LIKE :query%")
	List<User> findByFirstNameOrLastNameOrLoginId(@Param("query") String query);
}
