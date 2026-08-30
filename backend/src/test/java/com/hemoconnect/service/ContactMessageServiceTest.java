package com.hemoconnect.service;

import com.hemoconnect.dto.ContactMessageCreateDto;
import com.hemoconnect.entity.ContactCategory;
import com.hemoconnect.entity.ContactMessage;
import com.hemoconnect.entity.ContactPriority;
import com.hemoconnect.entity.ContactStatus;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.ContactMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactMessageServiceTest {

    @Mock
    private ContactMessageRepository contactMessageRepository;

    @InjectMocks
    private ContactMessageService contactMessageService;

    @Test
    void submit_savesMessageWithNewStatus() {
        when(contactMessageRepository.save(any(ContactMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactMessageCreateDto dto = new ContactMessageCreateDto();
        dto.setName("Asha Rao");
        dto.setEmail("asha@example.com");
        dto.setSubject("Trouble logging in");
        dto.setMessage("I can't reset my password.");
        dto.setCategory(ContactCategory.TECHNICAL_ISSUE);
        dto.setPriority(ContactPriority.MEDIUM);

        var result = contactMessageService.submit(dto);

        assertThat(result.getStatus()).isEqualTo(ContactStatus.NEW);
        assertThat(result.getEmail()).isEqualTo("asha@example.com");
    }

    @Test
    void updateStatus_changesStatusAndSaves() {
        ContactMessage existing = new ContactMessage();
        existing.setStatus(ContactStatus.NEW);
        when(contactMessageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(contactMessageRepository.save(any(ContactMessage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var result = contactMessageService.updateStatus(1L, ContactStatus.RESOLVED);

        assertThat(result.getStatus()).isEqualTo(ContactStatus.RESOLVED);

        ArgumentCaptor<ContactMessage> captor = ArgumentCaptor.forClass(ContactMessage.class);
        verify(contactMessageRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ContactStatus.RESOLVED);
    }

    @Test
    void getById_throwsResourceNotFoundException_whenMessageDoesNotExist() {
        when(contactMessageRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contactMessageService.getById(404L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
