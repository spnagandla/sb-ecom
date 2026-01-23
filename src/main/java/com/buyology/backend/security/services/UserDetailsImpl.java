package com.buyology.backend.security.services;

import com.buyology.backend.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@NoArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String userName;
    private String email;

    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String userName, String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.authorities = authorities;
    }

    // we need the static builder method to convert our CustomUser Object into userDetails object
    // Goal here is : Convert your database User object into a Spring-Security-friendly UserDetails object. ❌ does NOT understand your User entity ✅ DOES understand UserDetails
    public static UserDetailsImpl build(User user) {
        List<? extends GrantedAuthority> authorities = Optional.ofNullable(user.getRoles())
                .orElse(Collections.emptySet())
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleName().name()))
                .toList();

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return userName;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    //Why we override equals() Because different objects can represent the same real-world entity
    //Example: same user, same DB ID, loaded twice
    //We override equals() to check logical equality (by id, not by address)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }


    //Role of hashCode(): hashCode() is mainly used by hash-based collections HashMap, HashSet
    //It helps Java quickly locate objects Collections use:
    //hashCode() → find bucket , equals() → confirm match
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}


//To exclude a field from Java serialization, you must use transient ex:private transient String password
//@JsonIgnore prevents a field from being included in JSON serialization but does not affect Java serialization or Spring Security internals.