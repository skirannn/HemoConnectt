package com.hemoconnect.service;

import com.hemoconnect.entity.BloodGroup;

import java.util.Map;
import java.util.Set;

import static com.hemoconnect.entity.BloodGroup.*;

/**
 * Real ABO + Rh blood donation compatibility rules (who is medically
 * allowed to donate to whom), as a simple lookup table.
 *
 * This isn't the original project's exact matching logic (which appears
 * to have done a plain "same blood group" match) - but it's not
 * complicated either: it's one static map, no algorithm to speak of, and
 * it makes the matching results medically correct instead of
 * unnecessarily narrow (e.g. an O- donor genuinely can donate to
 * literally anyone, and a real app should surface that).
 *
 * Reference (standard transfusion compatibility chart):
 *   O-  can donate to: everyone
 *   O+  can donate to: O+, A+, B+, AB+
 *   A-  can donate to: A-, A+, AB-, AB+
 *   A+  can donate to: A+, AB+
 *   B-  can donate to: B-, B+, AB-, AB+
 *   B+  can donate to: B+, AB+
 *   AB- can donate to: AB-, AB+
 *   AB+ can donate to: AB+ only
 */
public final class BloodCompatibility {

    private BloodCompatibility() {
        // utility class - never instantiated
    }

    // recipient blood group -> the set of donor blood groups that can give to them
    private static final Map<BloodGroup, Set<BloodGroup>> DONORS_FOR_RECIPIENT = Map.of(
            O_NEGATIVE, Set.of(O_NEGATIVE),
            O_POSITIVE, Set.of(O_NEGATIVE, O_POSITIVE),
            A_NEGATIVE, Set.of(O_NEGATIVE, A_NEGATIVE),
            A_POSITIVE, Set.of(O_NEGATIVE, O_POSITIVE, A_NEGATIVE, A_POSITIVE),
            B_NEGATIVE, Set.of(O_NEGATIVE, B_NEGATIVE),
            B_POSITIVE, Set.of(O_NEGATIVE, O_POSITIVE, B_NEGATIVE, B_POSITIVE),
            AB_NEGATIVE, Set.of(O_NEGATIVE, A_NEGATIVE, B_NEGATIVE, AB_NEGATIVE),
            AB_POSITIVE, Set.of(BloodGroup.values()) // AB+ can receive from everyone
    );

    /** Which donor blood groups are medically compatible for this recipient's blood group. */
    public static Set<BloodGroup> compatibleDonorGroups(BloodGroup recipientBloodGroup) {
        return DONORS_FOR_RECIPIENT.get(recipientBloodGroup);
    }
}
