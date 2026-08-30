package com.hemoconnect.dto;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.BloodRequest;
import com.hemoconnect.entity.RequestStatus;
import com.hemoconnect.entity.UrgencyLevel;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** What we send back for a single blood request. */
public class BloodRequestResponseDto {

    private Long id;
    private Long requesterId;
    private String requesterName;
    private BloodGroup bloodGroup;
    private int unitsRequired;
    private String hospital;
    private String location;
    private UrgencyLevel urgency;
    private LocalDate requiredDate;
    private String description;
    private RequestStatus status;
    private Long confirmedDonorId;
    private String confirmedDonorName;
    private LocalDateTime expiresAt;
    private List<DonorResponseDto> responses;
    private LocalDateTime createdAt;
    private boolean flagged;
    private String flagReason;

    public static BloodRequestResponseDto fromEntity(BloodRequest request) {
        BloodRequestResponseDto dto = new BloodRequestResponseDto();
        dto.id = request.getId();
        dto.requesterId = request.getRequester().getId();
        dto.requesterName = request.getRequester().getName();
        dto.bloodGroup = request.getBloodGroup();
        dto.unitsRequired = request.getUnitsRequired();
        dto.hospital = request.getHospital();
        dto.location = request.getLocation();
        dto.urgency = request.getUrgency();
        dto.requiredDate = request.getRequiredDate();
        dto.description = request.getDescription();
        dto.status = request.getStatus();
        if (request.getConfirmedDonor() != null) {
            dto.confirmedDonorId = request.getConfirmedDonor().getId();
            dto.confirmedDonorName = request.getConfirmedDonor().getName();
        }
        dto.expiresAt = request.getExpiresAt();
        dto.responses = request.getResponses().stream()
                .map(DonorResponseDto::fromEntity)
                .toList();
        dto.createdAt = request.getCreatedAt();
        dto.flagged = request.isFlagged();
        dto.flagReason = request.getFlagReason();
        return dto;
    }

    // ----- Getters and setters -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(Long requesterId) {
        this.requesterId = requesterId;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public BloodGroup getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(BloodGroup bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public int getUnitsRequired() {
        return unitsRequired;
    }

    public void setUnitsRequired(int unitsRequired) {
        this.unitsRequired = unitsRequired;
    }

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UrgencyLevel getUrgency() {
        return urgency;
    }

    public void setUrgency(UrgencyLevel urgency) {
        this.urgency = urgency;
    }

    public LocalDate getRequiredDate() {
        return requiredDate;
    }

    public void setRequiredDate(LocalDate requiredDate) {
        this.requiredDate = requiredDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public Long getConfirmedDonorId() {
        return confirmedDonorId;
    }

    public void setConfirmedDonorId(Long confirmedDonorId) {
        this.confirmedDonorId = confirmedDonorId;
    }

    public String getConfirmedDonorName() {
        return confirmedDonorName;
    }

    public void setConfirmedDonorName(String confirmedDonorName) {
        this.confirmedDonorName = confirmedDonorName;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public List<DonorResponseDto> getResponses() {
        return responses;
    }

    public void setResponses(List<DonorResponseDto> responses) {
        this.responses = responses;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getFlagReason() {
        return flagReason;
    }

    public void setFlagReason(String flagReason) {
        this.flagReason = flagReason;
    }
}
