package org.acssz.membership.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Validated
@ConfigurationProperties(prefix = "app.storage.student-id")
@Getter
@Setter
public class StudentIdStorageProperties {

    private boolean enabled = false;

    @NotBlank(message = "student-id bucket must be configured when storage is enabled")
    private String bucket = "acssz-membership";

    private String endpoint;

    /**
     * AWS style region, for example "auto" when targeting GCS, or eu-west-1.
     */
    private String region = "auto";

    private String accessKey;

    private String secretKey;

    private String keyPrefix = "student-id";
}
