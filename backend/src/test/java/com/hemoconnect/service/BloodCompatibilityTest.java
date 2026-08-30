package com.hemoconnect.service;

import org.junit.jupiter.api.Test;

import static com.hemoconnect.entity.BloodGroup.*;
import static org.assertj.core.api.Assertions.assertThat;

class BloodCompatibilityTest {

    @Test
    void oNegative_canOnlyDonateToONegativeRecipients() {
        assertThat(BloodCompatibility.compatibleDonorGroups(O_NEGATIVE))
                .containsExactlyInAnyOrder(O_NEGATIVE);
    }

    @Test
    void abPositive_canReceiveFromEveryBloodGroup() {
        assertThat(BloodCompatibility.compatibleDonorGroups(AB_POSITIVE))
                .containsExactlyInAnyOrder(BloodGroupValues());
    }

    @Test
    void aPositive_canReceiveFromO_andA_groupsOnly() {
        assertThat(BloodCompatibility.compatibleDonorGroups(A_POSITIVE))
                .containsExactlyInAnyOrder(O_NEGATIVE, O_POSITIVE, A_NEGATIVE, A_POSITIVE);
    }

    @Test
    void bNegative_canReceiveOnlyFromONegativeAndBNegative() {
        assertThat(BloodCompatibility.compatibleDonorGroups(B_NEGATIVE))
                .containsExactlyInAnyOrder(O_NEGATIVE, B_NEGATIVE);
    }

    private static com.hemoconnect.entity.BloodGroup[] BloodGroupValues() {
        return com.hemoconnect.entity.BloodGroup.values();
    }
}
