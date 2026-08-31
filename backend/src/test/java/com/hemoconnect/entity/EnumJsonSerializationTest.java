package com.hemoconnect.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Module 9: verifies the custom @JsonValue/@JsonCreator behavior on Role
 * and BloodGroup actually produces the JSON shape the existing React
 * frontend expects (lowercase roles, "A+"-style blood groups) - this is
 * the exact mechanism that lets the old frontend work against this new
 * backend with no field-mapping code of its own.
 */
class EnumJsonSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void role_serializesAsLowercaseString() throws Exception {
        String json = objectMapper.writeValueAsString(Role.DONOR);
        assertThat(json).isEqualTo("\"donor\"");
    }

    @Test
    void role_deserializesFromLowercaseString() throws Exception {
        Role role = objectMapper.readValue("\"recipient\"", Role.class);
        assertThat(role).isEqualTo(Role.RECIPIENT);
    }

    @Test
    void bloodGroup_serializesWithPlusMinusLabel() throws Exception {
        String json = objectMapper.writeValueAsString(BloodGroup.O_NEGATIVE);
        assertThat(json).isEqualTo("\"O-\"");
    }

    @Test
    void bloodGroup_deserializesFromPlusMinusLabel() throws Exception {
        BloodGroup group = objectMapper.readValue("\"AB+\"", BloodGroup.class);
        assertThat(group).isEqualTo(BloodGroup.AB_POSITIVE);
    }
}
