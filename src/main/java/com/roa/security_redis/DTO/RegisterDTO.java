package com.roa.security_redis.DTO;

public record RegisterDTO(
        String email,
        String password,
        String first_name,
        String last_name
) {
}
