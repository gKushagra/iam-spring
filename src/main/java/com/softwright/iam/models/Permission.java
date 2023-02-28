package com.softwright.iam.models;

import jakarta.persistence.*;

@Entity
@Table(name="permissions")
public class Permission {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private Long userId;
	
	@Column(nullable=false)
	private Long orgId;
	
	@Column(nullable=false)
	private Long orgRuleId;

}
