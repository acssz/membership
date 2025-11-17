package org.acssz.membership.member;

/**
 * Encapsulates user-provided profile data for persistence.
 */
public record MemberProfileUpdate(
        OccupationType occupationType,
        StudentDegree studentDegree,
        byte[] studentIdCard,
        String studentIdCardContentType,
        String studentIdCardFilename) {
}
