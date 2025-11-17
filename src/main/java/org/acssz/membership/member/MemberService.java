package org.acssz.membership.member;

import java.time.Instant;
import java.util.Optional;

import org.acssz.membership.storage.StudentIdCardStorageService;
import org.acssz.membership.storage.StudentIdCardStorageService.StudentIdObjectLocation;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberRepository repository;
    private final StudentIdCardStorageService studentIdCardStorageService;

    private static final String STORAGE_DATABASE = "DATABASE";
    private static final String STORAGE_OBJECT = "OBJECT_STORE";

    public MemberService(MemberRepository repository, StudentIdCardStorageService studentIdCardStorageService) {
        this.repository = repository;
        this.studentIdCardStorageService = studentIdCardStorageService;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findBySubject(String subject) {
        return repository.findById(subject);
    }

    @Transactional
    public Member getOrCreate(String subject, String displayName) {
        return getOrCreate(subject, displayName, null);
    }

    @Transactional
    public Member getOrCreate(String subject, String displayName, String email) {
        return repository.findById(subject)
                .map(existing -> refreshProfileIfNeeded(existing, displayName, email))
                .orElseGet(() -> repository.save(new Member(subject, displayName, email)));
    }

    @Transactional
    public Member updateProfile(String subject, MemberProfileUpdate update) {
        Member member = repository.findById(subject)
                .orElseThrow(() -> new IllegalStateException("Member missing for subject " + subject));
        member.setOccupationType(update.occupationType());

        if (update.occupationType() == OccupationType.STUDENT) {
            member.setStudentDegree(update.studentDegree());
            if (hasNewStudentCard(update)) {
                persistStudentEvidence(member, update);
            }
        } else {
            clearStudentEvidence(member);
        }

        return repository.save(member);
    }

    private Member refreshProfileIfNeeded(Member member, String displayName, String email) {
        if (StringUtils.hasText(displayName) && !displayName.equals(member.getDisplayName())) {
            member.setDisplayName(displayName);
        }
        if (StringUtils.hasText(email)) {
            String existingEmail = member.getEmail();
            if (existingEmail == null || !email.equalsIgnoreCase(existingEmail)) {
                member.setEmail(email);
            }
        }
        return member;
    }

    private boolean hasNewStudentCard(MemberProfileUpdate update) {
        return update.studentIdCard() != null && update.studentIdCard().length > 0;
    }

    private void persistStudentEvidence(Member member, MemberProfileUpdate update) {
        if (studentIdCardStorageService.isEnabled()) {
            StudentIdObjectLocation location = studentIdCardStorageService
                    .store(member.getSubject(), update.studentIdCard(), update.studentIdCardContentType(),
                            update.studentIdCardFilename())
                    .orElseThrow(() -> new IllegalStateException("Storage returned empty location"));
            member.setStudentIdCard(null);
            member.setStudentIdCardContentType(null);
            member.setStudentIdCardObjectKey(location.objectKey());
            member.setStudentIdCardStorageType(STORAGE_OBJECT);
        } else {
            member.setStudentIdCard(update.studentIdCard());
            member.setStudentIdCardContentType(update.studentIdCardContentType());
            member.setStudentIdCardObjectKey(null);
            member.setStudentIdCardStorageType(STORAGE_DATABASE);
        }
        member.setStudentVerifiedAt(Instant.now());
    }

    private void clearStudentEvidence(Member member) {
        member.setStudentDegree(null);
        member.setStudentIdCard(null);
        member.setStudentIdCardContentType(null);
        member.setStudentIdCardObjectKey(null);
        member.setStudentIdCardStorageType(null);
        member.setStudentVerifiedAt(null);
    }
}
