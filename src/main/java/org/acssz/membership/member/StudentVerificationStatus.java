package org.acssz.membership.member;

import java.time.Instant;

public record StudentVerificationStatus(
        boolean required,
        boolean verified,
        boolean expired,
        Instant verifiedAt,
        Instant expiresAt) {

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public boolean isActive() {
        return !required || (verified && !expired);
    }

    public boolean requiresEvidenceUpload() {
        return required && (!verified || expired);
    }
}
