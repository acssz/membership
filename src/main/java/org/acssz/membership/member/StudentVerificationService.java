package org.acssz.membership.member;

import java.time.Duration;
import java.time.Instant;

import org.acssz.membership.config.VerificationProperties;
import org.springframework.stereotype.Service;

@Service
public class StudentVerificationService {

    private final VerificationProperties verificationProperties;

    public StudentVerificationService(VerificationProperties verificationProperties) {
        this.verificationProperties = verificationProperties;
    }

    public StudentVerificationStatus evaluate(Member member) {
        boolean required = member.getOccupationType() == OccupationType.STUDENT;
        Instant verifiedAt = member.getStudentVerifiedAt();
        Instant expiresAt = null;
        boolean verified = required && verifiedAt != null;
        boolean expired = false;

        if (verified) {
            expiresAt = verifiedAt.plus(verificationProperties.getStudentValidity());
            expired = Instant.now().isAfter(expiresAt);
        }

        return new StudentVerificationStatus(required, verified, expired, verifiedAt, expiresAt);
    }

    public Duration getStudentValidity() {
        return verificationProperties.getStudentValidity();
    }
}

