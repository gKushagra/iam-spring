package com.softwright.iam.repository;

import com.softwright.iam.models.Role;
import com.softwright.iam.models.Roles;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

@Transactional()
public interface RoleRepository extends JpaRepository<Role, Long> {

    @Query(value = "SELECT * FROM Role WHERE user_id = :id AND role = :role", nativeQuery = true)
    Role findAdminRole(@Param("id") UUID id, @Param("role") Roles role);
}
