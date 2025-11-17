package org.acssz.membership.member;

import java.time.Instant;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Minimal member profile persisted locally using authentik subject as ID.
 */
@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {

    @Id
    @Column(nullable = false, updatable = false, length = 128)
    private String subject;

    @Column(nullable = false, length = 160)
    private String displayName;

    @Column(length = 160)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private OccupationType occupationType;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private StudentDegree studentDegree;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] studentIdCard;

    @Column(length = 128)
    private String studentIdCardContentType;

    @Column(length = 64)
    private String studentIdCardStorageType;

    @Column(length = 512)
    private String studentIdCardObjectKey;

    private Instant studentVerifiedAt;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Member(String subject, String displayName) {
        this(subject, displayName, null);
    }

    public Member(String subject, String displayName, String email) {
        this.subject = subject;
        this.displayName = displayName;
        this.email = email;
    }
}
