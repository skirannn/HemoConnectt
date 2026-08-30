package com.hemoconnect.entity;

/**
 * The three notification triggers called out in the project brief:
 *   - a new request matches a donor's profile
 *   - a donor responded to a recipient's request
 *   - a request's status changed (confirmed / fulfilled / cancelled)
 */
public enum NotificationType {
    NEW_MATCHING_REQUEST,
    DONOR_RESPONSE,
    STATUS_CHANGE
}
