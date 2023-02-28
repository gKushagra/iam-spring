package com.softwright.iam.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.softwright.iam.models.User;

@Transactional(readOnly=true)
public interface UserRepository extends JpaRepository<User, Long> {
	
	Boolean existsByEmail(String email);
	
}
