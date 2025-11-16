package org.acssz.membership.qr;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "membership_qr_tokens", indexes = {
        @Index(name = "idx_tokens_member", columnList = "memberSubject")
})
public class MembershipQrToken {

    @Id
    @Column(nullable = false, length = 64)
    private String token;

    @Column(nullable = false, length = 128)
    private String memberSubject;

    @Column(nullable = false)
    private Instant issuedAt;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    public MembershipQrToken() {
    }

    public MembershipQrToken(String token, String memberSubject, Instant issuedAt, Instant expiresAt) {
        this.token = token;
        this.memberSubject = memberSubject;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMemberSubject() {
        return memberSubject;
    }

    public void setMemberSubject(String memberSubject) {
        this.memberSubject = memberSubject;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
