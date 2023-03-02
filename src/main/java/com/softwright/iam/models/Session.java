package com.softwright.iam.models;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name="sessions")
public class Session {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private UUID id;
	
	@Column(nullable=false)
	private UUID userId;
	
	@Column(nullable=false)
	private Date created;
	
	@Column(nullable=false)
	private Date expiry;
	
	public Session() {}

	public Session(UUID id, UUID userId, Date created, Date expiry) {
		this.id = id;
		this.userId = userId;
		this.created = created;
		this.expiry = expiry;
	}
	
	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getExpiry() {
		return expiry;
	}

	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

}
