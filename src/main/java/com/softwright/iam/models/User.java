package com.softwright.iam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="users", uniqueConstraints= {
		@UniqueConstraint(columnNames="email")
})
public class User {
	
	@Id
	@Column(nullable=false)
	private UUID id;
	
	@Column(nullable=false)
	private String email;
	
	@Column(nullable=false)
	private String hash;
	
	@Column(nullable=false)
	private String firstName;
	
	@Column(nullable=false)
	private String lastName;
	
	@Column(nullable=false)
	private LocalDateTime created;
	
	@Column(nullable=false)
	private LocalDateTime lastUpdated;
	
	public User() {}
	
	public User(String email, String hash, String firstName, String lastName) {
		this.id = UUID.randomUUID();
		this.email = email;
		this.hash = hash;
		this.firstName = firstName;
		this.lastName = lastName;
		this.created = LocalDateTime.now();
		this.lastUpdated = LocalDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public void setCreated(LocalDateTime created) {
		this.created = created;
	}

	public LocalDateTime getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(LocalDateTime lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
