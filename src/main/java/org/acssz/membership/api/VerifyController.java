package org.acssz.membership.api;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.acssz.membership.member.Member;
import org.acssz.membership.member.MemberService;
import org.acssz.membership.member.StudentVerificationService;
import org.acssz.membership.member.StudentVerificationStatus;
import org.acssz.membership.qr.MembershipQrToken;
import org.acssz.membership.qr.MembershipQrTokenService;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class VerifyController {

    private final MembershipQrTokenService tokenService;
    private final MemberService memberService;
    private final StudentVerificationService studentVerificationService;

    public VerifyController(MembershipQrTokenService tokenService, MemberService memberService,
            StudentVerificationService studentVerificationService) {
        this.tokenService = tokenService;
        this.memberService = memberService;
        this.studentVerificationService = studentVerificationService;
    }

    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(@RequestParam("token") String token) {
        if (!StringUtils.hasText(token)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("valid", false, "message", "token-missing"));
        }
        return tokenService.validateToken(token)
                .map(validToken -> ResponseEntity.ok(buildValidResponse(validToken)))
                .orElseGet(() -> ResponseEntity.ok(Map.of("valid", false)));
    }

    private Map<String, Object> buildValidResponse(MembershipQrToken token) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valid", true);
        response.put("memberSubject", token.getMemberSubject());
        response.put("expiresAt", token.getExpiresAt().toString());

        Optional<Member> memberOptional = memberService.findBySubject(token.getMemberSubject());
        memberOptional.ifPresent(member -> {
            StudentVerificationStatus status = studentVerificationService.evaluate(member);
            Map<String, Object> profile = new LinkedHashMap<>();
            profile.put("displayName", member.getDisplayName());
            profile.put("occupation", member.getOccupationType());
            profile.put("studentDegree", member.getStudentDegree());
            profile.put("studentVerificationActive", status.isActive());
            profile.put("studentVerificationExpiresAt",
                    status.expiresAt() != null ? status.expiresAt().toString() : null);
            response.put("profile", profile);
        });
        return response;
    }
}
