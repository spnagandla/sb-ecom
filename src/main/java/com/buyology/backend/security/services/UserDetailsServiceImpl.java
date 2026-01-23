package com.buyology.backend.security.services;

import com.buyology.backend.model.User;
import com.buyology.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        return userRepository.findByUserName(username)
                .map(UserDetailsImpl::build) // Db will return the user(Model) but springSecurity understand the userdetails type so we are building the userDetails out of user.
                .orElseThrow(() ->
                        new UsernameNotFoundException("User Not Found with username: " + username)
                );
    }
}



//UserDetails represents the authenticated user’s security information, and UserDetailsService loads that user by username from the database; Spring Security itself validates the password.