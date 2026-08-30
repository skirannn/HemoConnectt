package com.hemoconnect.service;

import com.hemoconnect.entity.BloodRequest;
import com.hemoconnect.entity.RequestStatus;
import com.hemoconnect.repository.BloodRequestRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * A small background job that sweeps for expired requests once an hour,
 * so a request that nobody happens to view/list doesn't just sit there
 * forever showing PENDING even though its 30-day window has passed.
 *
 * This is optional on top of BloodRequestService's "lazy" expiry check
 * (which catches it the moment anyone views/lists the request) - but it's
 * a genuinely simple, standard Spring feature (@Scheduled) worth seeing:
 * one annotation, one method, no extra libraries.
 */
@Component
public class ExpiredRequestScheduler {

    private static final Logger log = LoggerFactory.getLogger(ExpiredRequestScheduler.class);

    private final BloodRequestRepository bloodRequestRepository;

    public ExpiredRequestScheduler(BloodRequestRepository bloodRequestRepository) {
        this.bloodRequestRepository = bloodRequestRepository;
    }

    /** Runs every hour (3,600,000 ms). See HemoConnectApplication for @EnableScheduling. */
    @Scheduled(fixedRate = 3_600_000)
    public void expireOldRequests() {
        List<BloodRequest> openRequests = bloodRequestRepository.findByStatusInOrderByCreatedAtDesc(
                List.of(RequestStatus.PENDING, RequestStatus.MATCHED));

        int expiredCount = 0;
        for (BloodRequest request : openRequests) {
            if (request.isExpired()) {
                request.setStatus(RequestStatus.EXPIRED);
                bloodRequestRepository.save(request);
                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            log.info("Expired {} blood request(s) past their 30-day window", expiredCount);
        }
    }
}
