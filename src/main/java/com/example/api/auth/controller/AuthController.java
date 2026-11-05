package com.example.api.auth.controller;

import com.example.api.auth.RoleContext;
import com.example.api.auth.TenantContext;
import com.example.api.auth.entity.AppUser;
import com.example.api.auth.entity.Tenant;
import com.example.api.auth.repository.AppUserRepository;
import com.example.api.auth.repository.TenantRepository;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final AppUserRepository userRepository;

    private final TenantRepository tenantRepository;
    private final boolean cookieSecure;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtEncoder jwtEncoder,
            AppUserRepository userRepository,
            TenantRepository tenantRepository,
            @Value("${security.cookie.secure:true}") boolean cookieSecure) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.cookieSecure = cookieSecure;
    }

    public record LoginRequest(
            String email,
            String password
    ) {}

    public record TenantInfo(UUID id, String name) {}

    public record WhoAmIResponse(
            String email,
            TenantInfo tenant,
            List<String> roles
    ) {}

    @GetMapping("/whoami")
    public WhoAmIResponse whoami(@AuthenticationPrincipal Jwt jwt) {
        UUID tenantId = TenantContext.getTenantId();
        String tenantName = tenantRepository.findById(tenantId)
                .map(Tenant::getName)
                .orElseThrow();

        return new WhoAmIResponse(
                jwt.getSubject(),
                new TenantInfo(tenantId, tenantName),
                RoleContext.getRoles()
        );
    }

    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    @PostMapping("/login")
    public ResponseEntity<Void> login(
            @RequestBody LoginRequest request) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(),
                                request.password()
                        )
                );

        AppUser user = userRepository
                .findByEmail(authentication.getName())
                .orElseThrow();

        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("mi-aplicacion")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(900))
                .subject(user.getEmail())
                .claim("scope", user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName())
                        .collect(Collectors.joining(" ")))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofSeconds(900))
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
