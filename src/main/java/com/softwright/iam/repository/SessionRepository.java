package com.softwright.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import com.softwright.iam.models.Session;
import java.util.UUID;

@Transactional(readOnly=true)
public interface SessionRepository extends JpaRepository<Session, UUID> {
		
	Session findByUserId(UUID userId);
}
