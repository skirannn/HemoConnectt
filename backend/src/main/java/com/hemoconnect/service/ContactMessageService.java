package com.hemoconnect.service;

import com.hemoconnect.dto.ContactMessageCreateDto;
import com.hemoconnect.dto.ContactMessageResponseDto;
import com.hemoconnect.entity.ContactMessage;
import com.hemoconnect.entity.ContactStatus;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.ContactMessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for the public Contact Us form and its admin-side
 * review queue.
 */
@Service
public class ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    public ContactMessageService(ContactMessageRepository contactMessageRepository) {
        this.contactMessageRepository = contactMessageRepository;
    }

    /** Anyone can submit - no login required (see SecurityConfig). */
    public ContactMessageResponseDto submit(ContactMessageCreateDto dto) {
        ContactMessage message = new ContactMessage();
        message.setName(dto.getName());
        message.setEmail(dto.getEmail());
        message.setPhone(dto.getPhone());
        message.setSubject(dto.getSubject());
        message.setMessage(dto.getMessage());
        message.setCategory(dto.getCategory());
        message.setPriority(dto.getPriority());
        // status defaults to NEW

        ContactMessage saved = contactMessageRepository.save(message);
        return ContactMessageResponseDto.fromEntity(saved);
    }

    /** Admin-only: every message, or filtered to one status if provided. */
    public List<ContactMessageResponseDto> listAll(ContactStatus statusFilter) {
        List<ContactMessage> messages = statusFilter != null
                ? contactMessageRepository.findByStatusOrderByCreatedAtDesc(statusFilter)
                : contactMessageRepository.findAllByOrderByCreatedAtDesc();

        return messages.stream()
                .map(ContactMessageResponseDto::fromEntity)
                .toList();
    }

    public ContactMessageResponseDto getById(Long id) {
        return ContactMessageResponseDto.fromEntity(findOrThrow(id));
    }

    public ContactMessageResponseDto updateStatus(Long id, ContactStatus newStatus) {
        ContactMessage message = findOrThrow(id);
        message.setStatus(newStatus);
        ContactMessage saved = contactMessageRepository.save(message);
        return ContactMessageResponseDto.fromEntity(saved);
    }

    private ContactMessage findOrThrow(Long id) {
        return contactMessageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contact message not found with id: " + id));
    }
}
