package com.hemoconnect.entity;

/**
 * The lifecycle a BloodRequest moves through.
 *
 * This is a simplified version of the original project's status machine
 * (which also had a separate 'in_progress' state between 'matched' and
 * 'confirmed'). We merged that into CONFIRMED — once a specific donor is
 * confirmed, the request IS in progress; a separate state for the same
 * real-world situation just adds a state to track without adding real
 * meaning.
 *
 *   PENDING    -> a donor ACCEPTs           -> MATCHED
 *   MATCHED    -> requester confirms a donor -> CONFIRMED
 *   CONFIRMED  -> donation actually happens  -> FULFILLED
 *   (any state) -> requester cancels         -> CANCELLED
 *   (any active state) -> 30 days pass       -> EXPIRED
 */
public enum RequestStatus {
    PENDING,
    MATCHED,
    CONFIRMED,
    FULFILLED,
    CANCELLED,
    EXPIRED
}
