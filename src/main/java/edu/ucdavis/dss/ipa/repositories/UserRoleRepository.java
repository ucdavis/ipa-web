package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.UserRole;
import org.springframework.data.repository.query.Param;

public interface UserRoleRepository extends CrudRepository<UserRole, Long> {

	List<UserRole> findByWorkgroupId(long id);

	@Query("FROM UserRole ur WHERE ur.user.loginId = :loginId")
	List<UserRole> findByLoginId(@Param("loginId") String loginId);

	List<UserRole> findByUserLoginId(String loginId);
}
