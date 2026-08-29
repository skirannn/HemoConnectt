package com.hemoconnect.entity;

/**
 * The three roles a HemoConnect user can have.
 *
 * In the original MERN project this was a plain string field on the User
 * model ('donor' | 'recipient' | 'admin'). Using a Java enum instead means
 * the compiler stops us from ever typing an invalid role by mistake, and
 * Spring Data JPA stores it as a readable string in MySQL (see
 * @Enumerated(EnumType.STRING) on User.role).
 */
public enum Role {
    DONOR,
    RECIPIENT,
    ADMIN
}
