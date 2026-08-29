package com.hemoconnect.entity;

/**
 * The 8 standard human blood groups.
 *
 * The original Mongoose schema restricted this field with a plain
 * `enum: ['A+', 'A-', 'B+', ...]` array. MySQL/Java identifiers can't
 * contain '+' or '-', so we spell them out (A_POSITIVE) and expose the
 * familiar "A+" form to the frontend through the display label below.
 */
public enum BloodGroup {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-");

    private final String label;

    BloodGroup(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
