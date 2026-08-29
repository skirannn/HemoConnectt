package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * What the frontend sends us when a user edits their profile
 * (equivalent to PUT /api/users/profile in the original Express app,
 * and part of the /profile-setup flow).
 *
 * Every field is validated with Jakarta Bean Validation annotations.
 * Spring automatically rejects an invalid request with a 400 error
 * BEFORE it ever reaches our service/controller code, as long as the
 * controller method parameter is annotated with @Valid.
 */
public class UserUpdateRequestDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be under 100 characters")
    private String name;

    @Pattern(regexp = "^[0-9+\\-\\s]{7,15}$", message = "Enter a valid phone number")
    private String phone;

    private BloodGroup bloodGroup;

    @NotBlank(message = "Location is required")
    private String location;

    private boolean availableForDonation;

    public UserUpdateRequestDto() {
    }

    // ----- Getters and setters -----

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public boolean isAvailableForDonation() {
        return availableForDonation;
    }

    public void setAvailableForDonation(boolean availableForDonation) {
        this.availableForDonation = availableForDonation;
    }
}
