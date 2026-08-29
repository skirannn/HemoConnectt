package com.hemoconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * The User entity — the Java/JPA equivalent of the original
 * server/models/User.js Mongoose schema.
 *
 * One row in the `users` table represents one account, regardless of
 * whether that account is a donor, a recipient, or an admin (same design
 * as the original project: a single collection/table with a `role` flag,
 * rather than three separate tables). Donor-specific medical/eligibility
 * data lives in a separate DonorProfile entity (Module 3), exactly like
 * the original project kept a separate DonorBlood collection.
 *
 * @Entity tells Hibernate "this class maps to a database table".
 * @Table lets us name that table explicitly.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    /** Always a BCrypt hash — never store a plain-text password. */
    @Column(nullable = false)
    private String password;

    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "blood_group", nullable = false)
    private BloodGroup bloodGroup;

    @Column(nullable = false)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Whether a DONOR is currently willing to be matched with requests.
     * Meaningless for RECIPIENT/ADMIN accounts, same as isAvailable was
     * in the original User model (used only when role === 'donor').
     */
    @Column(name = "available_for_donation")
    private boolean availableForDonation = true;

    /**
     * Mirrors the original `profileComplete` flag. The React
     * ProtectedRoute component redirects a logged-in user to
     * /profile-setup whenever this is false — we preserve that rule.
     */
    @Column(name = "profile_completed")
    private boolean profileCompleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User() {
        // JPA requires a no-argument constructor.
    }

    /**
     * @PrePersist runs automatically right before Hibernate inserts a new
     * row, so we never have to remember to set these timestamps manually.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /** Runs automatically right before Hibernate updates an existing row. */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ----- Getters and setters -----
    // We use plain getters/setters (no Lombok) on purpose: it keeps the
    // class readable for anyone new to Java, with no "magic" annotations
    // generating code you can't see.

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isAvailableForDonation() {
        return availableForDonation;
    }

    public void setAvailableForDonation(boolean availableForDonation) {
        this.availableForDonation = availableForDonation;
    }

    public boolean isProfileCompleted() {
        return profileCompleted;
    }

    public void setProfileCompleted(boolean profileCompleted) {
        this.profileCompleted = profileCompleted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
