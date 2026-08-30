package com.hemoconnect.dto;

import com.hemoconnect.entity.ContactCategory;
import com.hemoconnect.entity.ContactPriority;
import jakarta.validation.constraints.*;

/** What the public Contact Us form sends to POST /api/contact. */
public class ContactMessageCreateDto {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    private String phone;

    @NotBlank(message = "Subject is required")
    @Size(max = 200)
    private String subject;

    @NotBlank(message = "Message is required")
    @Size(max = 2000)
    private String message;

    @NotNull(message = "Category is required")
    private ContactCategory category;

    @NotNull(message = "Priority is required")
    private ContactPriority priority;

    public ContactMessageCreateDto() {
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

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ContactCategory getCategory() {
        return category;
    }

    public void setCategory(ContactCategory category) {
        this.category = category;
    }

    public ContactPriority getPriority() {
        return priority;
    }

    public void setPriority(ContactPriority priority) {
        this.priority = priority;
    }
}
