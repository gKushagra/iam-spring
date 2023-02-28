package com.softwright.iam.models;

import jakarta.persistence.*;

@Entity
@Table(name="rules")
public class Rule {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private Long orgId;
	
	@Column(nullable=false)
	private String ruleShortName;
	
	@Column(nullable=true)
	private String description;

}
