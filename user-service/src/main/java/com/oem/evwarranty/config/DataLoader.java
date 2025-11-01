package com.oem.evwarranty.config;


import com.oem.evwarranty.entity.User;
import com.oem.evwarranty.entity.UserStatus;
import com.oem.evwarranty.repository.RoleRepository;
import com.oem.evwarranty.repository.UserRepository;
import com.oem.evwarranty.entity.Role;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(UserRepository userRepository,
                               RoleRepository roleRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {

            // Danh sách role cần tạo
            String[] roles = {"ADMIN", "SC_STAFF", "SC_TECHNICIAN", "EVM_STAFF","MANAGER"};

            // Tạo role nếu chưa tồn tại
            for (String roleName : roles) {
                roleRepository.findByRoleName(roleName)
                        .orElseGet(() -> roleRepository.save(new Role(roleName)));
            }

            // Tạo user mặc định nếu chưa tồn tại
            createUserIfNotExist(userRepository, roleRepository, passwordEncoder,
                    "Admin", "admin123", "ADMIN");
            createUserIfNotExist(userRepository, roleRepository, passwordEncoder,
                    "SCStaff", "staff123", "SC_STAFF");
            createUserIfNotExist(userRepository, roleRepository, passwordEncoder,
                    "SCTechnician", "tech123", "SC_TECHNICIAN");
            createUserIfNotExist(userRepository, roleRepository, passwordEncoder,
                    "EVMStaff", "evm123", "EVM_STAFF");
            createUserIfNotExist(userRepository, roleRepository, passwordEncoder,
                    "Manager", "manager123", "MANAGER");
        };
    }

    private void createUserIfNotExist(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      PasswordEncoder passwordEncoder,
                                      String username,
                                      String rawPassword,
                                      String roleName) {

        if (userRepository.findByUsername(username).isEmpty()) {
            // Lấy role từ DB, nếu chưa có thì tạo
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseGet(() -> roleRepository.save(new Role(roleName)));

            Set<Role> roles = new HashSet<>();
            roles.add(role);

            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setStatus(UserStatus.ACTIVE);
            user.setRoles(roles);

            userRepository.save(user);
        }
    }
}
