package com.softwright.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.softwright.iam.models.Session;

import java.util.Date;
import java.util.UUID;

@Transactional(readOnly=true)
public interface SessionRepository extends JpaRepository<Session, UUID> {
	@Query(value = "SELECT * FROM sessions WHERE user_id = :userId AND expiry > :expiry", nativeQuery = true)
	Session findByUserIdAndLessThanExpiry(@Param("userId") UUID userId, @Param("expiry") Date expiry);
}
