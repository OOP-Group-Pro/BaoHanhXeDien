package com.oem.evwarranty.repository;

import com.oem.evwarranty.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> { // sửa Long
    Optional<Role> findByRoleName(String roleName);
}
