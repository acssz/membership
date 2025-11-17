package org.acssz.membership.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Validated
@ConfigurationProperties(prefix = "app.verification")
@Getter
@Setter
public class VerificationProperties {

    @NotNull
    private Duration studentValidity = Duration.ofDays(365);
}
