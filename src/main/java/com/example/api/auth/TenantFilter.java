package com.example.api.auth;
import com.example.api.auth.entity.AppUser;
import com.example.api.auth.repository.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final AppUserRepository userRepository;

    public TenantFilter(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        return request.getServletPath()
                .equals("/api/auth/login");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        try {
            Authentication authentication =
                    SecurityContextHolder.getContext()
                            .getAuthentication();

            if (authentication == null
                    || !authentication.isAuthenticated()
                    || !(authentication.getPrincipal()
                    instanceof Jwt jwt)) {
                response.sendError(401);
                return;
            }

            String email = jwt.getSubject();

            AppUser user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null) {
                response.sendError(401);
                return;
            }

            TenantContext.setTenantId(user.getTenantId());

            chain.doFilter(request, response);

        } finally {
            TenantContext.clear();
        }
    }
}