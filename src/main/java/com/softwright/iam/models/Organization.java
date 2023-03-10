package com.softwright.iam.models;

import jakarta.persistence.*;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name="organizations")
public class Organization {

    @Id
    @Column(nullable=false)
    private UUID id;

    @Column(nullable=false)
    private String name;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable=false)
    private Date created;

    @Column(nullable=false)
    private Date lastUpdated;

    public Organization() {}

    public Organization(String name, UUID userId) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.userId = userId;
        this.created = new Date();
        this.lastUpdated = new Date();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
