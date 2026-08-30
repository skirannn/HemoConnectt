package com.hemoconnect.security;

import com.hemoconnect.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security doesn't know anything about our `User` entity - it works
 * with its own `UserDetails` interface. UserPrincipal is a small adapter
 * that wraps a `User` and answers the questions Spring Security asks
 * (What's the username? What's the password hash? What roles/authorities
 * does this user have?).
 *
 * We prefix the role with "ROLE_" because that's the convention Spring
 * Security's hasRole("ADMIN") style checks expect internally.
 */
public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    /** Lets other classes (e.g. controllers) get back the real User/id easily. */
    public User getUser() {
        return user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        // We log in with email, so "username" (Spring Security's term) is
        // our email field.
        return user.getEmail();
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
}
