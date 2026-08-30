package com.hemoconnect.repository;

import com.hemoconnect.entity.ContactMessage;
import com.hemoconnect.entity.ContactStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findAllByOrderByCreatedAtDesc();

    List<ContactMessage> findByStatusOrderByCreatedAtDesc(ContactStatus status);

    // Used by AdminService for the dashboard's "new messages" count.
    long countByStatus(ContactStatus status);
}
