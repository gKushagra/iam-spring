package com.softwright.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.softwright.iam.models.User;

import java.util.Date;
import java.util.UUID;

@Transactional()
public interface UserRepository extends JpaRepository<User, Long> {
	
	Boolean existsByEmail(String email);
	
	User findByEmail(String email);

	User findById(UUID id);

	@Modifying
	@Query(value = "UPDATE User SET hash = :hash, lastUpdated = :lastUpdated WHERE id = :id")
	void updateHashAndLastUpdated(@Param("hash") String hash, @Param("lastUpdated") Date lastUpdated, @Param("id") UUID id);

	@Modifying
	@Query(value = "UPDATE User SET email = :email, firstName = :firstName, lastName = :lastName, lastUpdated = :lastUpdated WHERE id = :id")
	void updateUser(@Param("email") String email, @Param("firstName") String firstName, @Param("lastName") String lastName,
					@Param("lastUpdated") Date lastUpdated, @Param("id") UUID userId);
}
