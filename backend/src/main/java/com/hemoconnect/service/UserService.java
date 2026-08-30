package com.hemoconnect.service;

import com.hemoconnect.dto.UserResponseDto;
import com.hemoconnect.dto.UserUpdateRequestDto;
import com.hemoconnect.entity.User;
import com.hemoconnect.exception.DuplicateResourceException;
import com.hemoconnect.exception.ResourceNotFoundException;
import com.hemoconnect.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service = the layer that holds business logic.
 *
 * The controller should never talk to the repository directly — it always
 * goes through the service. That way, if a rule changes (for example "an
 * email must be unique"), there's exactly one place to change it, and it's
 * easy to unit-test without spinning up a whole web server (see
 * UserServiceTest in Module 20).
 *
 * We use CONSTRUCTOR INJECTION (a single constructor, no @Autowired
 * needed) rather than field injection — it's the Spring team's own
 * recommended style and makes dependencies explicit and easy to test.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Creates a new user with a securely hashed password.
     * Package-private-ish "internal" use for now — AuthService (Module 2)
     * will call this during registration. We're building it here because
     * user creation is fundamentally a USER concern, not an AUTH concern.
     */
    public User createUser(User newUser) {
        if (userRepository.existsByEmail(newUser.getEmail())) {
            throw new DuplicateResourceException(
                    "An account with email " + newUser.getEmail() + " already exists");
        }
        // Never store the raw password - always hash it first.
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        return userRepository.save(newUser);
    }

    public UserResponseDto getUserById(Long id) {
        User user = findUserOrThrow(id);
        return UserResponseDto.fromEntity(user);
    }

    /**
     * Returns the raw User entity (not a DTO) by email. Used internally by
     * AuthService, which needs the real entity - e.g. to read the password
     * hash during login, or to build the {token, user} response after a
     * successful login/signup.
     */
    public User getUserEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Returns the raw User entity by id. Used internally by other services
     * (e.g. DonorProfileService) that need to attach a related entity
     * (like a DonorProfile) to a real User object, not just a DTO.
     */
    public User getUserEntityById(Long id) {
        return findUserOrThrow(id);
    }

    /**
     * Overwrites a user's password with a freshly hashed value. Used by
     * AuthService's OTP-based "forgot password" flow.
     */
    public void updatePassword(String email, String newRawPassword) {
        User user = getUserEntityByEmail(email);
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UserResponseDto::fromEntity)
                .toList();
    }

    /**
     * Updates the editable profile fields for a user. This is the Java
     * equivalent of PUT /api/users/profile and the profile-setup flow in
     * the original app: it also flips profileCompleted to true, the same
     * business rule ProtectedRoute.jsx relies on to stop redirecting the
     * user to /profile-setup.
     */
    public UserResponseDto updateProfile(Long id, UserUpdateRequestDto request) {
        User user = findUserOrThrow(id);

        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBloodGroup(request.getBloodGroup());
        user.setLocation(request.getLocation());
        user.setAvailableForDonation(request.isAvailableForDonation());
        user.setProfileCompleted(true);

        User saved = userRepository.save(user);
        return UserResponseDto.fromEntity(saved);
    }

    public void deleteUser(Long id) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
    }

    /**
     * Small private helper so "find by id or throw a clean 404" isn't
     * repeated in every public method above.
     */
    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }
}
