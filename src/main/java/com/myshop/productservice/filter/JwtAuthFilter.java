package com.myshop.productservice.filter;

import com.myshop.productservice.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            log.error("Invalid JWT Token");
            return;
        }

        String token = authHeader.substring(7);


        try {
            Claims claims = jwtService.validateToken(token);

            String username = claims.getSubject();
            List<String> roles = claims.get("roles", List.class);
            Collection<GrantedAuthority> authorities = roles == null ? Collections.emptyList() :
                    roles.stream()
                            .map(role -> "ROLE_" + role.toUpperCase()) // Spring ожидает "ROLE_USER"
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

            // Устанавливаем аутентификацию
            Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            System.out.println("Authenticated: " + auth.isAuthenticated());
            System.out.println("Authorities: " + auth.getAuthorities());
            System.out.println("Has ROLE_USER? " + auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
            log.info("success authentication");
        } catch (RuntimeException e) {
            log.error("Invalid JWT Token", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        log.info("Next filter chain");
        chain.doFilter(request, response);
        log.info("end filter chain");

    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/app/me");
    }
}