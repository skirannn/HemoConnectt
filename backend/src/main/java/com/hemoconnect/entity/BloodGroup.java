package com.hemoconnect.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The 8 standard human blood groups.
 *
 * The original Mongoose schema restricted this field with a plain
 * `enum: ['A+', 'A-', 'B+', ...]` array. MySQL/Java identifiers can't
 * contain '+' or '-', so the Java constant names spell them out
 * (A_POSITIVE) - but @JsonValue/@JsonCreator below make this enum still
 * SERIALIZE and DESERIALIZE over the API as the familiar "A+"/"O-" style
 * strings, so the existing React frontend (Module 9) can keep sending and
 * displaying blood groups exactly the way it always did, with no changes
 * needed on that side.
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

    /** @JsonValue: whenever Jackson serializes this enum to JSON, use this label ("A+") instead of the Java constant name. */
    @JsonValue
    public String getLabel() {
        return label;
    }

    /** @JsonCreator: whenever Jackson deserializes JSON into this enum, accept the label ("A+") instead of requiring the Java constant name. */
    @JsonCreator
    public static BloodGroup fromLabel(String label) {
        for (BloodGroup group : values()) {
            if (group.label.equalsIgnoreCase(label) || group.name().equalsIgnoreCase(label)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Unknown blood group: " + label);
    }
}
