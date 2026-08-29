package com.hemoconnect.service;

import com.hemoconnect.entity.BloodGroup;
import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.DuplicateResourceException;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * A unit test for UserService. "Unit" means we test UserService in
 * complete isolation - we don't start a real Spring app or talk to a
 * real database. Instead, we give UserService a FAKE (mocked)
 * UserRepository and PasswordEncoder and check that it behaves correctly.
 *
 * @ExtendWith(MockitoExtension.class) turns on Mockito's annotations
 * (@Mock, @InjectMocks) for this test class.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("Asha Rao");
        sampleUser.setEmail("asha@example.com");
        sampleUser.setPassword("plainPassword123");
        sampleUser.setBloodGroup(BloodGroup.O_POSITIVE);
        sampleUser.setLocation("Hyderabad");
        sampleUser.setRole(Role.DONOR);
    }

    @Test
    void createUser_hashesPasswordAndSaves_whenEmailIsNew() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plainPassword123")).thenReturn("hashed-value");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = userService.createUser(sampleUser);

        assertThat(saved.getPassword()).isEqualTo("hashed-value");
        verify(userRepository).save(sampleUser);
    }

    @Test
    void createUser_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("asha@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(sampleUser))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("asha@example.com");

        // Password should never be hashed/saved if the email is a duplicate.
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_throwsResourceNotFoundException_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getUserById_returnsDtoWithoutPassword_whenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

        var result = userService.getUserById(1L);

        assertThat(result.getEmail()).isEqualTo("asha@example.com");
        assertThat(result.getName()).isEqualTo("Asha Rao");
        // UserResponseDto has no getPassword() at all - that's the point:
        // it's structurally impossible to leak the password hash.
    }
}
