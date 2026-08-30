package com.hemoconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Runs once per incoming HTTP request, BEFORE it reaches any controller.
 *
 * This is the direct Spring Security equivalent of the original Express
 * `authenticateToken` middleware: pull the "Bearer <token>" header off the
 * request, verify it, look up the user, and — if everything checks out —
 * tell Spring "this request is authenticated as this user". If there's no
 * token, or it's invalid, we simply don't authenticate and let the request
 * continue; SecurityConfig's authorization rules decide afterwards whether
 * an unauthenticated request is allowed to reach that particular endpoint.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // strip off "Bearer "

        try {
            String email = jwtService.extractEmail(token);

            // Only authenticate if nobody has already authenticated this
            // request in this thread (avoids redundant work).
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtService.isTokenValid(token, userDetails.getUsername())) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Invalid/expired token, or the user it points to no longer
            // exists. We deliberately don't throw here - we just leave the
            // request unauthenticated and let SecurityConfig's rules
            // decide what happens next (usually a clean 401/403).
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
