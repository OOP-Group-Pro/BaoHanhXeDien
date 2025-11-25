package com.oem.evuser.mapper;

import com.oem.evuser.dto.RoleDto;
import com.oem.evuser.entity.Role;

public class RoleMapper {
    public static Role mapToRole (RoleDto roleDto) {
        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        return role;
    }

    public static RoleDto mapToRoleDto (Role role) {
        return new RoleDto(
                role.getRoleId(),
                role.getRoleName()
        );
    }
}
