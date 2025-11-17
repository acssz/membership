package org.acssz.membership.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.acssz.membership.config.StudentIdStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;

@Service
public class StudentIdCardStorageService {

    private static final Logger log = LoggerFactory.getLogger(StudentIdCardStorageService.class);

    private final StudentIdStorageProperties properties;
    private volatile MinioClient client;

    public StudentIdCardStorageService(StudentIdStorageProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled()
                && StringUtils.hasText(properties.getBucket())
                && StringUtils.hasText(properties.getEndpoint())
                && StringUtils.hasText(properties.getAccessKey())
                && StringUtils.hasText(properties.getSecretKey());
    }

    public Optional<StudentIdObjectLocation> store(String memberSubject, byte[] content, String contentType, String filename) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        if (content == null || content.length == 0) {
            return Optional.empty();
        }

        MinioClient minioClient = client;
        if (minioClient == null) {
            synchronized (this) {
                if (client == null) {
                    client = initClient();
                }
                minioClient = client;
            }
        }

        String key = buildObjectKey(memberSubject, filename);
        String resolvedContentType = StringUtils.hasText(contentType) ? contentType : "application/octet-stream";
        String bucket = properties.getBucket();
        if (!StringUtils.hasText(bucket)) {
            log.error("Student ID storage bucket not configured, skipping upload");
            return Optional.empty();
        }

        try (InputStream stream = new ByteArrayInputStream(content)) {
            log.info("Uploading student ID for subject {} to bucket {} with key {} via {}",
                    memberSubject,
                    bucket,
                    key,
                    properties.getEndpoint());
            log.info("Using access key {} and prefix {}", properties.getAccessKey(), properties.getKeyPrefix());
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(key)
                    .stream(stream, content.length, -1)
                    .contentType(resolvedContentType)
                    .build();
            minioClient.putObject(args);
            return Optional.of(new StudentIdObjectLocation(key, resolvedContentType, Instant.now(), bucket));
        } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException exception) {
            log.error("Failed to upload student id to bucket {} at endpoint {} with key {}",
                    bucket,
                    properties.getEndpoint(),
                    key,
                    exception);
            throw new IllegalStateException("Cannot upload student ID image at the moment", exception);
        }
    }

    private MinioClient initClient() {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }

    private String buildObjectKey(String memberSubject, String filename) {
        String sanitizedFilename = (filename != null && !filename.isBlank())
                ? filename.replaceAll("[^a-zA-Z0-9._-]", "_")
                : "student-id.png";
        return "%s/%s/%s-%s".formatted(
                properties.getKeyPrefix(),
                memberSubject,
                UUID.randomUUID(),
                sanitizedFilename);
    }

    public record StudentIdObjectLocation(String objectKey, String contentType, Instant uploadedAt, String bucket) {
    }
}
