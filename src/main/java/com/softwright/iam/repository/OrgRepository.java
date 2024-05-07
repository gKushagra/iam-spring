package com.softwright.iam.repository;

import com.softwright.iam.models.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional()
public interface OrgRepository extends JpaRepository<Organization, Long> {

    Boolean existsByNameAndUserId(String email, UUID userId);

    Organization findById(UUID id);

    Organization findByNameAndUserId(String name, UUID id);
}
