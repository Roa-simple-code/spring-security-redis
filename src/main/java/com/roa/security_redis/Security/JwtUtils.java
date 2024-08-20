package com.roa.security_redis.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JwtUtils {

    @Value("${secret_key}")
    private String SECRET_KEY;
    private final Date ISSUED_DATE = Date.from(Instant.now());
    private final Date ACCESS_EXP_DATE = Date.from(Instant.now().plus(10, ChronoUnit.MINUTES));
    private SecretKey secretKey(){
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserDetails userDetails){

        HashMap<String,Object> claims = new HashMap<>();
        claims.put("authorities",userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer("https://roa.io")
                .signWith(secretKey())
                .issuedAt(ISSUED_DATE)
                .expiration(ACCESS_EXP_DATE)
                .compact();
                    }

    public String generateRefreshToken(HashMap<String,Object> extraClaims,UserDetails userDetails){

        HashMap<String,Object> authorities = new HashMap<>();
        authorities.put("authorities",userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claims(extraClaims)
                .claim("authorities",authorities)
                .subject(userDetails.getUsername())
                .issuer("https://roa.io")
                .signWith(secretKey())
                .issuedAt(ISSUED_DATE)
                .expiration(ACCESS_EXP_DATE)
                .compact();
    }

    private Claims extractAllClaims(String jwt){
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(jwt)
                .getPayload();
    }

    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    public Date extractExpDate(String token){
        return extractAllClaims(token).getExpiration();
    }

    public boolean isTokenExp(String token){
        return extractExpDate(token).before(Date.from(Instant.now()));
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        String email = extractEmail(token);
        return email.equals(userDetails.getUsername()) && !isTokenExp(token);
    }

}
