package org.acssz.membership.qr;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MembershipQrTokenRepository extends JpaRepository<MembershipQrToken, String> {

    long deleteByExpiresAtBefore(Instant instant);

    long deleteByMemberSubjectAndExpiresAtBefore(String memberSubject, Instant instant);
}
