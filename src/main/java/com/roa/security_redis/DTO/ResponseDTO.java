package com.roa.security_redis.DTO;

import com.roa.security_redis.entity.Roles;
import lombok.Builder;

import java.util.List;

@Builder
public record ResponseDTO(
        String fullName,
        String email,
        List<Roles> roles,
        String access_token,
        String refresh_token){}
