package org.acssz.membership.config;

import org.acssz.membership.member.MemberService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            OidcUserService oidcUserService) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/manifest.json", "/service-worker.js", "/icons/**", "/css/**", "/js/**")
                        .permitAll()
                        .requestMatchers("/api/verify").permitAll()
                        .anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/api/verify")))
                .oauth2Login(oauth -> oauth.userInfoEndpoint(userInfo -> userInfo.oidcUserService(oidcUserService)))
                .logout(logout -> logout.logoutSuccessUrl("/"));
        return http.build();
    }

    @Bean
    public OidcUserService oidcUserService(MemberService memberService) {
        OidcUserService delegate = new OidcUserService();
        return new OidcUserService() {
            @Override
            public OidcUser loadUser(OidcUserRequest userRequest) {
                OidcUser oidcUser = delegate.loadUser(userRequest);
                registerMember(memberService, oidcUser);
                return oidcUser;
            }
        };
    }

    private void registerMember(MemberService memberService, OidcUser oidcUser) {
        String displayName = oidcUser.getFullName();
        if (displayName == null) {
            displayName = oidcUser.getPreferredUsername();
        }
        if (displayName == null) {
            displayName = oidcUser.getEmail();
        }
        if (displayName == null) {
            displayName = oidcUser.getSubject();
        }
        memberService.getOrCreate(oidcUser.getSubject(), displayName, oidcUser.getEmail());
    }
}
