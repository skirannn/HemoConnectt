package com.hemoconnect.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The three roles a HemoConnect user can have.
 *
 * In the original MERN project this was a plain lowercase string field on
 * the User model ('donor' | 'recipient' | 'admin'). We use a Java enum
 * internally (the compiler stops us from ever typing an invalid role by
 * mistake), but @JsonValue/@JsonCreator below make it serialize/deserialize
 * over the API as that same familiar lowercase string - so the existing
 * React frontend's role checks (e.g. `user.role === 'donor'`) keep working
 * with no changes needed (Module 9).
 *
 * Note: Spring Security's own authority string (e.g. "ROLE_DONOR", built
 * from name() in UserPrincipal) is unaffected by these JSON annotations -
 * .name() always returns the Java constant name regardless of how the
 * enum is serialized to JSON.
 */
public enum Role {
    DONOR,
    RECIPIENT,
    ADMIN;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Role fromJson(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}
