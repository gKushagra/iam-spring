package com.softwright.iam.models;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name="logs")
public class Log {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private String log;
	
	@Column(nullable=false)
	private LocalDateTime timestamp;

}
