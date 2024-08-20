package com.roa.security_redis.Security;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private final String BLACKLISTED_TOKEN = "blacklisted_token:";
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;
    private final    TokenService tokenService;

    @Autowired
    public JwtFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService, TokenService tokenService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) {

        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            if(authHeader == null || !authHeader.contains("Bearer ")){
                filterChain.doFilter(request,response);
                return;
            }
            String jwt = authHeader.substring(7);
            String userEmail = jwtUtils.extractEmail(jwt);
            if(userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if(jwtUtils.isTokenValid(jwt,userDetails) && isTokenBlacklisted(jwt)){
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }filterChain.doFilter(request,response);

        }catch (ServletException | IOException exception){
            throw new RuntimeException(exception);
        }
    }
    private  boolean isTokenBlacklisted(String jwt) {
        String key = BLACKLISTED_TOKEN + jwt;
        return tokenService.getToken(key) == null;
    }

}
