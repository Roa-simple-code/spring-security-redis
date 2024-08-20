package com.roa.security_redis.repo;

import com.roa.security_redis.entity.Roles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepo extends JpaRepository<Roles,Long> {

    Roles findByRoleName(String roleName);

}
