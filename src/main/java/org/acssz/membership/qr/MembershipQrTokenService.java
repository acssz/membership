package org.acssz.membership.qr;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;

@Service
public class MembershipQrTokenService {

    private final MembershipQrTokenRepository repository;
    private final long tokenTtlSeconds;

    public MembershipQrTokenService(MembershipQrTokenRepository repository,
            @Value("${app.qr.token-ttl-seconds:30}") long tokenTtlSeconds) {
        this.repository = repository;
        this.tokenTtlSeconds = tokenTtlSeconds;
    }

    @Transactional
    public byte[] generateTokenPng(String memberSubject) {
        MembershipQrToken token = issueToken(memberSubject);
        return renderTokenToPng(token.getToken());
    }

    @Transactional(readOnly = true)
    public Optional<MembershipQrToken> validateToken(String tokenValue) {
        return repository.findById(tokenValue)
                .filter(token -> !token.isExpired() && !token.isUsed());
    }

    @Transactional
    public void markTokenUsed(String tokenValue) {
        repository.findById(tokenValue).ifPresent(token -> {
            token.setUsed(true);
            repository.save(token);
        });
    }

    private MembershipQrToken issueToken(String memberSubject) {
        Instant now = Instant.now();
        repository.deleteByMemberSubjectAndExpiresAtBefore(memberSubject, now.minusSeconds(10));
        repository.deleteByExpiresAtBefore(now.minusSeconds(120));

        String tokenValue = UUID.randomUUID().toString().replace("-", "");
        MembershipQrToken token = new MembershipQrToken(tokenValue, memberSubject, now,
                now.plusSeconds(tokenTtlSeconds));
        return repository.save(token);
    }

    private byte[] renderTokenToPng(String tokenValue) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);
            BitMatrix bitMatrix = new QRCodeWriter().encode(
                    tokenValue, BarcodeFormat.QR_CODE, 220, 220, hints);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(MatrixToImageWriter.toBufferedImage(bitMatrix), "PNG", out);
            return out.toByteArray();
        } catch (WriterException | java.io.IOException e) {
            throw new IllegalStateException("Failed to build QR image", e);
        }
    }
}
