package com.softwright.iam.models;

import java.util.Date;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name="sessions")
public class Session {
	
	@Id
	private UUID id;
	
	@Column(nullable=false)
	private UUID userId;
	
	@Column(nullable=false)
	private Date created;
	
	@Column(nullable=false)
	private Date expiry;

	@Column()
	private boolean isResetSession;

	@Column
	private boolean resetProcessed;
	
	public Session() {}

	public Session(UUID userId, Date created, Date expiry) {
		this.id = UUID.randomUUID();
		this.userId = userId;
		this.created = created;
		this.expiry = expiry;
		this.isResetSession = false;
		this.resetProcessed = false;
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

	public boolean isResetSession() {
		return isResetSession;
	}

	public void setResetSession(boolean resetSession) {
		isResetSession = resetSession;
	}

	public boolean isResetProcessed() {
		return resetProcessed;
	}

	public void setResetProcessed(boolean resetProcessed) {
		this.resetProcessed = resetProcessed;
	}
}
