package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.User;

import java.time.LocalDateTime;

/**
 * DTO = Data Transfer Object.
 *
 * Why do we need this instead of just returning the User entity directly
 * from the controller? Two reasons:
 *   1. Security — User has a `password` field (a BCrypt hash). We must
 *      NEVER let that field leave the backend in a JSON response.
 *   2. Stability — the JSON shape we send to React can stay the same even
 *      if we later rename or restructure fields inside the User entity.
 *
 * This is a plain, flat class: no logic, just fields + a factory method
 * that copies the safe fields out of a User.
 */
public class UserResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private BloodGroup bloodGroup;
    private String location;
    private Role role;
    private boolean availableForDonation;
    private boolean profileCompleted;
    private LocalDateTime createdAt;

    public UserResponseDto() {
    }

    /** Converts a User entity into a safe response DTO. */
    public static UserResponseDto fromEntity(User user) {
        UserResponseDto dto = new UserResponseDto();
        dto.id = user.getId();
        dto.name = user.getName();
        dto.email = user.getEmail();
        dto.phone = user.getPhone();
        dto.bloodGroup = user.getBloodGroup();
        dto.location = user.getLocation();
        dto.role = user.getRole();
        dto.availableForDonation = user.isAvailableForDonation();
        dto.profileCompleted = user.isProfileCompleted();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    // ----- Getters and setters -----

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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
