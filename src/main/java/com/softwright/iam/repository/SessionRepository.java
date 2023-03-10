package com.softwright.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.softwright.iam.models.Session;

import java.util.Date;
import java.util.UUID;

@Transactional()
public interface SessionRepository extends JpaRepository<Session, UUID> {
	@Query(value = "SELECT * FROM sessions WHERE user_id = :userId AND expiry > :expiry AND is_reset_session = false", nativeQuery = true)
	Session findByUserIdAndLessThanExpiry(@Param("userId") UUID userId, @Param("expiry") Date expiry);

	@Query(value = "SELECT * FROM sessions WHERE id = :id AND expiry > :expiry", nativeQuery = true)
	Session findByIdAndLessThanExpiry(@Param("id") UUID id, @Param("expiry") Date expiry);

	@Modifying
	@Query(value = "UPDATE Session SET resetProcessed = :resetProcessed WHERE id = :id")
	void updateResetProcessed(@Param("resetProcessed") boolean resetProcessed, @Param("id") UUID id);
}
