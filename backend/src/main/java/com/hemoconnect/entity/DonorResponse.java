package com.hemoconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One donor's response to one BloodRequest. In the original MongoDB model
 * this was an embedded sub-document array on BloodRequest; in a relational
 * database, "an array of objects on another object" becomes its own table
 * with a foreign key back to the parent — that's exactly what @ManyToOne
 * below does.
 */
@Entity
@Table(name = "donor_responses")
public class DonorResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "blood_request_id", nullable = false)
    private BloodRequest bloodRequest;

    @ManyToOne
    @JoinColumn(name = "donor_id", nullable = false)
    private User donor;

    @Enumerated(EnumType.STRING)
    @Column(name = "response_type", nullable = false)
    private ResponseType responseType;

    @Column(name = "response_message", length = 500)
    private String responseMessage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public DonorResponse() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
    }

    public BloodRequest getBloodRequest() {
        return bloodRequest;
    }

    public void setBloodRequest(BloodRequest bloodRequest) {
        this.bloodRequest = bloodRequest;
    }

    public User getDonor() {
        return donor;
    }

    public void setDonor(User donor) {
        this.donor = donor;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
