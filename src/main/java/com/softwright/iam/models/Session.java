package com.softwright.iam.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="sessions")
public class Session {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private Long userId;
	
	@Column(nullable=false)
	private LocalDateTime created;
	
	@Column(nullable=false)
	private LocalDateTime expiry;

}
