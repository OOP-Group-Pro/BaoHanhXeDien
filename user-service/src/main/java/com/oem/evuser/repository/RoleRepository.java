package com.oem.evuser.repository;

import com.oem.evuser.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> { // sửa Long
    Optional<Role> findByRoleName(String roleName);
}
