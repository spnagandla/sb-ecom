package com.buyology.backend.utils;

import com.buyology.backend.model.User;
import com.buyology.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    private final UserRepository userRepository;

    public AuthUtil(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new UsernameNotFoundException("No authenticated user found");
        }

        return userRepository.findByUserName(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found with username: " + auth.getName()
                ));
    }

    public String loggedInEmail() {
        return getAuthenticatedUser().getEmail();
    }

    public Long loggedInUserId() {
        return getAuthenticatedUser().getUserId();
    }

    public User loggedInUser() {
        return getAuthenticatedUser();
    }
}
