package com.softwright.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.softwright.iam.models.Organization;

@Transactional(readOnly=true)
public interface OrgRepository extends JpaRepository<Organization, Long> {

}
