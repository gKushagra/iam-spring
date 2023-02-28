package com.softwright.iam.models;

import jakarta.persistence.*;

@Entity
@Table(name="user_roles")
public class UserRole {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private Long userId;
	
	@Column(nullable=false)
	private Role role;

}
