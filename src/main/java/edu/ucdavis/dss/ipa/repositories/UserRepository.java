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

	@Query("SELECT u FROM User u WHERE u.loginId IN :loginIds")
	List<User> findByLoginIds(@Param("loginIds") List<String> loginIds);

	@Query( " SELECT DISTINCT u" +
			" FROM User u, TeachingAssignment ta, Schedule s, Budget b" +
			" WHERE b.id = :budgetId" +
			" AND b.schedule = s" +
			" AND ta.schedule = s" +
			" AND ta.instructor = u"
	)
	List<User> findByTeachingAssignments(@Param("budgetId") long budgetId);
}
