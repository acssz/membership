package org.acssz.membership.api;

import java.util.Map;

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

    public VerifyController(MembershipQrTokenService tokenService) {
        this.tokenService = tokenService;
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
        // TODO: 在接入 B 端业务时，这里可以附加会员等级、有效期等附加信息
        return Map.of(
                "valid", true,
                "memberSubject", token.getMemberSubject(),
                "expiresAt", token.getExpiresAt().toString());
    }
}
