package com.roa.security_redis.repo;

import com.roa.security_redis.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<Users,Long> {

    Optional<Users> findByEmail(String email);

}
