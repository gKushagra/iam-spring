package com.softwright.iam.models;

import jakarta.persistence.*;

@Entity
@Table(name="user_org_roles")
public class UserOrgRole {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private Long userId;
	
	@Column(nullable=false)
	private Long orgId;
	
	@Column(nullable=false)
	private Role role;

}
