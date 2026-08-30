package com.hemoconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A message submitted through the public Contact Us form.
 *
 * Not linked to a User with a foreign key on purpose - the whole point of
 * a "Contact Us" form is that someone doesn't need an account to reach
 * out (a donor with login trouble, a journalist, anyone). We just store
 * whatever contact details they typed in.
 *
 * NOTE: the original project actually had TWO contact-related models -
 * an unused stub called `Contact.js`, and the one that was really wired
 * into the app, `ContactMessage.js`. We only migrate the real one; the
 * unused stub isn't carried forward (see docs/analysis/EXISTING_PROJECT_ANALYSIS.md).
 */
@Entity
@Table(name = "contact_messages")
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    private String phone;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, length = 2000)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContactStatus status = ContactStatus.NEW;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public ContactMessage() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
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
}
