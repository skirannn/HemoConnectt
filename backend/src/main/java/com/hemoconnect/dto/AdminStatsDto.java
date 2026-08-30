package com.hemoconnect.dto;

/** Aggregate counts for the admin dashboard's overview cards. */
public class AdminStatsDto {

    private long totalUsers;
    private long totalDonors;
    private long totalRecipients;
    private long totalAdmins;
    private long activeRequests;    // PENDING or MATCHED
    private long fulfilledRequests; // real count, never mock data
    private long flaggedRequests;
    private long newContactMessages;

    public AdminStatsDto() {
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(long totalDonors) {
        this.totalDonors = totalDonors;
    }

    public long getTotalRecipients() {
        return totalRecipients;
    }

    public void setTotalRecipients(long totalRecipients) {
        this.totalRecipients = totalRecipients;
    }

    public long getTotalAdmins() {
        return totalAdmins;
    }

    public void setTotalAdmins(long totalAdmins) {
        this.totalAdmins = totalAdmins;
    }

    public long getActiveRequests() {
        return activeRequests;
    }

    public void setActiveRequests(long activeRequests) {
        this.activeRequests = activeRequests;
    }

    public long getFulfilledRequests() {
        return fulfilledRequests;
    }

    public void setFulfilledRequests(long fulfilledRequests) {
        this.fulfilledRequests = fulfilledRequests;
    }

    public long getFlaggedRequests() {
        return flaggedRequests;
    }

    public void setFlaggedRequests(long flaggedRequests) {
        this.flaggedRequests = flaggedRequests;
    }

    public long getNewContactMessages() {
        return newContactMessages;
    }

    public void setNewContactMessages(long newContactMessages) {
        this.newContactMessages = newContactMessages;
    }
}
