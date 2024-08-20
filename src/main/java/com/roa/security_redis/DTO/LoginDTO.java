package com.roa.security_redis.DTO;

public record LoginDTO(
        String email,
        String password
) {
}
