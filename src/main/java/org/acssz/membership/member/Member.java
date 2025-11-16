package org.acssz.membership.member;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Minimal member profile persisted locally using authentik subject as ID.
 */
@Entity
@Table(name = "members")
public class Member {

    @Id
    @Column(nullable = false, updatable = false, length = 128)
    private String subject;

    @Column(nullable = false, length = 160)
    private String displayName;

    @Column(length = 160)
    private String degree;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Member() {
    }

    public Member(String subject, String displayName) {
        this.subject = subject;
        this.displayName = displayName;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
