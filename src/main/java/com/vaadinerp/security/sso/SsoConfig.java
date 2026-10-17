package com.vaadinerp.security.sso;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.Set;

/** Merangkai verifikator logout_token dari konfigurasi {@code app.sso.*} dan penyedia kunci JWKS pada {@link SsoService}. */
@Configuration
class SsoConfig {

    @Bean
    LogoutTokenVerifier logoutTokenVerifier(SsoProperties props, SsoService sso) {
        Set<String> issuers = new LinkedHashSet<>();
        if (props.getIssuer() != null && !props.getIssuer().isBlank()) {
            issuers.add(props.getIssuer());
        }
        if (props.getLogoutAcceptedIssuers() != null) {
            props.getLogoutAcceptedIssuers().stream().filter(i -> i != null && !i.isBlank()).forEach(issuers::add);
        }
        return new LogoutTokenVerifier(issuers, props.getClientId(), Clock.systemUTC(), sso::rsaKey);
    }
}
