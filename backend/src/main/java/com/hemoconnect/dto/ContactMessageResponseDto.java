package com.hemoconnect.dto;

import com.hemoconnect.entity.ContactCategory;
import com.hemoconnect.entity.ContactMessage;
import com.hemoconnect.entity.ContactPriority;
import com.hemoconnect.entity.ContactStatus;

import java.time.LocalDateTime;

public class ContactMessageResponseDto {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private ContactCategory category;
    private ContactPriority priority;
    private ContactStatus status;
    private LocalDateTime createdAt;

    public static ContactMessageResponseDto fromEntity(ContactMessage entity) {
        ContactMessageResponseDto dto = new ContactMessageResponseDto();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.email = entity.getEmail();
        dto.phone = entity.getPhone();
        dto.subject = entity.getSubject();
        dto.message = entity.getMessage();
        dto.category = entity.getCategory();
        dto.priority = entity.getPriority();
        dto.status = entity.getStatus();
        dto.createdAt = entity.getCreatedAt();
        return dto;
    }

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

    public ContactStatus getStatus() {
        return status;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
