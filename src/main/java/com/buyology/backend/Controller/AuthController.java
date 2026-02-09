package com.buyology.backend.Controller;

import com.buyology.backend.repository.UserRepository;
import com.buyology.backend.security.jwt.JwtUtils;
import com.buyology.backend.security.request.LoginRequest;
import com.buyology.backend.security.request.SignupRequest;
import com.buyology.backend.security.response.MessageResponse;
import com.buyology.backend.security.response.UserInfoResponse;
import com.buyology.backend.security.services.SecurityService;
import com.buyology.backend.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    // Core Spring Security component that performs authentication
    // It delegates to AuthenticationProviders (DaoAuthenticationProvider in your case)
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    private final JwtUtils jwtUtils;
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);


    public AuthController(JwtUtils jwtUtils, AuthenticationManager authenticationManager, UserRepository userRepository, SecurityService securityService) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.securityService = securityService;
    }

    // Login endpoint (should be permitAll in security config)
    @PostMapping("/signin")
    public ResponseEntity<UserInfoResponse> authenticateUser(
            @RequestBody @Valid LoginRequest loginRequest) {

        /*
         * STEP 1: Authenticate username & password
         *
         * - We create a UsernamePasswordAuthenticationToken using raw credentials
         * - This token is passed to AuthenticationManager
         * - AuthenticationManager delegates to DaoAuthenticationProvider
         * - DaoAuthenticationProvider:
         *      → loads user via UserDetailsServiceImpl
         *      → compares passwords using PasswordEncoder
         * - If credentials are invalid → AuthenticationException is thrown
         * - If valid → returns an authenticated Authentication object
         */
        log.info("Authentication attempt for username: {}", loginRequest.getUserName());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUserName(),
                        loginRequest.getPassword()
                )
        );
        log.info("Authentication successful for username: {}", loginRequest.getUserName());

        /*
         * STEP 2: Extract the authenticated user
         *
         * - authentication.getPrincipal() returns the authenticated user object
         * - This is the UserDetails returned by UserDetailsServiceImpl
         * - We cast it to our custom UserDetailsImpl
         */
        UserDetailsImpl userDetails =
                (UserDetailsImpl) authentication.getPrincipal();

        /*
         * STEP 3: Generate JWT
         *
         * - Uses authenticated user information
         * - Signs the token
         * - Adds claims like username / roles (depending on your JwtUtils)
         * - This token will be sent by the client on every future request
         */
        ResponseCookie jwtCookie = jwtUtils.generateJwtCookie(userDetails);

        /*
         * STEP 4: Extract roles from UserDetails
         *
         * - Spring Security stores roles as GrantedAuthority
         * - We convert them to simple String values for the response
         * - Example: ROLE_USER, ROLE_ADMIN
         */
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        /*
         * STEP 5: Build and return response
         *
         * - Contains basic user info + JWT
         * - Client stores JWT (usually in memory or secure storage)
         * - JWT is sent in Authorization header for future requests
         */
        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles
        );

        log.info("Login response successfully created for username: {}",
                userDetails.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);

    }


    @PostMapping("/signup")
    public ResponseEntity<?> registerUser( @Valid @RequestBody SignupRequest signupRequest){

        securityService.register(signupRequest);
        return ResponseEntity.ok(
                new MessageResponse("User registered successfully!",true, Instant.now()));

    }

    @GetMapping("/user/username")
    public String currentUsename(Authentication authentication){
        return authentication != null ? authentication.getName() :"null";
    }

    @GetMapping("/user")
    public ResponseEntity<?> userDetails(Authentication authentication){
        //authentication.getPrincipal() returns a generic Object, and Java needs an explicit cast so you can access fields (id, etc.) that exist only in your custom user implementation.
        UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        UserInfoResponse response = new UserInfoResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                roles
        );
        return ResponseEntity.ok().body(response);
    }


    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {

        ResponseCookie deleteCookie = jwtUtils.getCleanJwtCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .body(new MessageResponse("Signed out successfully"));
    }

}

