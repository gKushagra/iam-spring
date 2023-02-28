package com.softwright.iam.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="orgs", uniqueConstraints = {
		@UniqueConstraint(columnNames="name")
})
public class Organization {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private String name;
	
	@Column(nullable=false)
	private LocalDateTime created;
	
	@Column(nullable=false)
	private LocalDateTime updated;

}
