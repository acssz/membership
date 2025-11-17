package org.acssz.membership;

import org.acssz.membership.config.StudentIdStorageProperties;
import org.acssz.membership.config.VerificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({ VerificationProperties.class, StudentIdStorageProperties.class })
public class MembershipApplication {

	public static void main(String[] args) {
		SpringApplication.run(MembershipApplication.class, args);
	}

}
