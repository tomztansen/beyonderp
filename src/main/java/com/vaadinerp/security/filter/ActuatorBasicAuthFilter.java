package com.vaadinerp.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Filter Keamanan Khusus Actuator (HTTP Basic Authentication).
 * Mencegat seluruh request yang berawalan "/actuator" dan meminta kredensial
 * melalui Popup Login standar browser. Request di luar "/actuator" akan
 * diteruskan 100% tanpa gangguan ke sistem Vaadin Flow/ERP.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class ActuatorBasicAuthFilter extends OncePerRequestFilter {

    @Value("${actuator.security.username:adminlog}")
    private String username;

    @Value("${actuator.security.password:LogSecret2026!}")
    private String password;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getRequestURI();
        // Hanya proteksi rute /actuator
        if (path != null && path.startsWith("/actuator")) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Basic ")) {
                String base64Credentials = authHeader.substring(6).trim();
                try {
                    byte[] credDecoded = Base64.getDecoder().decode(base64Credentials);
                    String credentials = new String(credDecoded, StandardCharsets.UTF_8);
                    // format adalah username:password
                    final String[] values = credentials.split(":", 2);
                    if (values.length == 2) {
                        String reqUser = values[0];
                        String reqPass = values[1];
                        if (this.username.equals(reqUser) && this.password.equals(reqPass)) {
                            // Kredensial cocok! Izinkan akses ke log/actuator
                            filterChain.doFilter(request, response);
                            return;
                        }
                    }
                } catch (Exception ignored) {
                    // Jika Base64 tidak valid, lanjut ke 401
                }
            }

            // Kredensial tidak ada atau salah: kirim 401 dan WWW-Authenticate agar browser memunculkan popup Basic Auth
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setHeader("WWW-Authenticate", "Basic realm=\"VaadinERP Actuator Security\"");
            response.getWriter().write("401 Unauthorized - Kredensial Actuator Tidak Valid");
            return;
        }

        // Untuk seluruh URL selain /actuator, lewatkan tanpa cek apa pun
        filterChain.doFilter(request, response);
    }
}
