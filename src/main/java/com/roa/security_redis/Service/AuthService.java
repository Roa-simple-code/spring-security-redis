package com.roa.security_redis.Service;

import com.roa.security_redis.DTO.LoginDTO;
import com.roa.security_redis.DTO.RegisterDTO;
import com.roa.security_redis.DTO.ResponseDTO;
import com.roa.security_redis.Security.JwtUtils;
import com.roa.security_redis.Security.TokenService;
import com.roa.security_redis.entity.Roles;
import com.roa.security_redis.entity.Users;
import com.roa.security_redis.repo.RoleRepo;
import com.roa.security_redis.repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RoleRepo roleRepo;
    private final UserRepo userRepo;
    private final TokenService tokenService;
    private final String REFRESH_TOKEN = "refresh_token:";
    private final String BLACKLISTED_TOKEN = "blacklisted_token:";
    @Autowired
    public AuthService(PasswordEncoder passwordEncoder, JwtUtils jwtUtils, RoleRepo roleRepo, UserRepo userRepo, TokenService tokenService) {
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.roleRepo = roleRepo;
        this.userRepo = userRepo;
        this.tokenService = tokenService;
    }

    public ResponseDTO register(RegisterDTO register){

        Roles role = roleRepo.findByRoleName("USER".toUpperCase());

        Users user = new Users();
        user.setEmail(register.email());
        user.setPassword(passwordEncoder.encode(register.password()));
        user.setFirst_name(register.first_name());
        user.setLast_name(register.last_name());
        user.setRoles(List.of(role));
        userRepo.save(user);

        String access_token = jwtUtils.generateAccessToken(user);
        String refresh_token = jwtUtils.generateRefreshToken(extraClaims(user),user);
        saveTokenToRedis(user,refresh_token);
        return ResponseDTO.builder()
                .email(user.getEmail())
                .fullName(user.getFullName())
                .refresh_token(refresh_token)
                .access_token(access_token)
                .roles(List.of(role))
                .build();

    }

    public ResponseDTO login(LoginDTO loginDTO){
        Users user = userRepo.findByEmail(loginDTO.email())
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));


        String access_token = jwtUtils.generateAccessToken(user);
        String refresh_token = jwtUtils.generateRefreshToken(extraClaims(user),user);
        saveTokenToRedis(user,refresh_token);
        return ResponseDTO.builder()
                .access_token(access_token)
                .refresh_token(refresh_token)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
    public ResponseDTO refresh(String access_token){

        String email = jwtUtils.extractEmail(access_token);
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        String refresh_token = tokenService.getToken(REFRESH_TOKEN + user.getId());
        if(refresh_token != null && jwtUtils.isTokenValid(refresh_token,user)
                && isTokenBlacklisted(refresh_token)){
            invalidate_token(refresh_token);

            String new_refresh_token = jwtUtils.generateRefreshToken(extraClaims(user),user);

            saveTokenToRedis(user,new_refresh_token);

        return ResponseDTO.builder()
                .access_token(access_token)
                .refresh_token(refresh_token)
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
        }else {
            return null;
        }
    }

    public void logout(String access_token){

        String email = jwtUtils.extractEmail(access_token);
        Users user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
        String refresh_token_key = REFRESH_TOKEN + user.getId();
        String refresh_token = tokenService.getToken(refresh_token_key);
        invalidate_token(access_token);
        invalidate_token(refresh_token);
    }

    private void saveTokenToRedis(Users user,String refresh_token){
        Long refresh_token_TTL = jwtUtils.extractExpDate(refresh_token).getTime();
        tokenService.saveToken(REFRESH_TOKEN + user.getId(),refresh_token,refresh_token_TTL);
    }

    private void invalidate_token(String refresh_token){
        Long refresh_token_TTL = jwtUtils.extractExpDate(refresh_token).getTime();
        tokenService.saveToken(BLACKLISTED_TOKEN + refresh_token,refresh_token,refresh_token_TTL);
    }

    public boolean isTokenBlacklisted(String refresh_token) {
        String key = BLACKLISTED_TOKEN + refresh_token;
    return tokenService.getToken(key) == null;
    }

    private HashMap<String,Object> extraClaims(Users users){
        HashMap<String,Object> extra = new HashMap<>();
        extra.put("full_name",users.getFullName());
        return extra;
    }

}
