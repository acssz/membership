package org.acssz.membership.member;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberRepository repository;

    public MemberService(MemberRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<Member> findBySubject(String subject) {
        return repository.findById(subject);
    }

    @Transactional
    public Member getOrCreate(String subject, String displayName) {
        return repository.findById(subject)
                .map(existing -> refreshDisplayNameIfNeeded(existing, displayName))
                .orElseGet(() -> repository.save(new Member(subject, displayName)));
    }

    @Transactional
    public Member updateDegree(String subject, String degree) {
        Member member = repository.findById(subject)
                .orElseThrow(() -> new IllegalStateException("Member missing for subject " + subject));
        member.setDegree(StringUtils.hasText(degree) ? degree.trim() : null);
        return repository.save(member);
    }

    private Member refreshDisplayNameIfNeeded(Member member, String displayName) {
        if (StringUtils.hasText(displayName) && !displayName.equals(member.getDisplayName())) {
            member.setDisplayName(displayName);
        }
        return member;
    }
}
