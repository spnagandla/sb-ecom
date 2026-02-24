package com.buyology.backend.security.services;

import com.buyology.backend.exception.APIException;
import com.buyology.backend.model.Role;
import com.buyology.backend.model.User;
import com.buyology.backend.model.UserRoles;
import com.buyology.backend.repository.RoleRepository;
import com.buyology.backend.repository.UserRepository;
import com.buyology.backend.security.request.SignupRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class SecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final Logger log = LoggerFactory.getLogger(SecurityService.class);

    public SecurityService(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public void register(SignupRequest request) {

        log.info("Signup attempt username='{}', email='{}'", request.getUserName(), request.getEmail());
        if (userRepository.existsByUserName(request.getUserName())) {
            throw new APIException("UserName Already Taken!");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new APIException("Email is already in use!");
        }

        User user = new User(
                request.getUserName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );

        user.setRoles(resolveRoles(request.getRole()));
        userRepository.save(user);
        log.info("Signup success for username'{}'", user.getUserName());
    }

    private Set<Role> resolveRoles(Set<String> requestRoles) {
        if (requestRoles == null || requestRoles.isEmpty()) {
            return Set.of(getRole(UserRoles.ROLE_USER));
        }
        Set<Role> roles = new HashSet<>();
        for (String role : requestRoles) {
            roles.add(mapRole(role)); // user sends the admin, seller and we map that to the ROLE_ADMIN..  before storing to Db
        }
        return roles;
    }

    private Role mapRole(String role) {
        String normalized = (role == null ? " " : role.trim().toLowerCase());
        return switch (normalized) {
            case "admin" -> getRole(UserRoles.ROLE_ADMIN);
            case "seller" -> getRole(UserRoles.ROLE_SELLER);
            default -> getRole(UserRoles.ROLE_USER);
        };
    }

    public Role getRole(UserRoles role) {
        return roleRepository.findByRoleName(role)
                .orElseThrow(() -> new APIException("Role not found:   Will assign the default role" + role));
    }

}
