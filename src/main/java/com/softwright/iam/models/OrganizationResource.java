package com.softwright.iam.models;

import jakarta.persistence.*;

@Entity
@Table(name="org_resources")
public class OrganizationResource {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private Long orgId;
	
	@Column(nullable=false)
	private String redirectUri;

}
