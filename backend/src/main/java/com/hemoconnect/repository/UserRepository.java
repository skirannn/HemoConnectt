package com.hemoconnect.repository;

import com.hemoconnect.entity.Role;
import com.hemoconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository = the layer that talks to the database.
 *
 * We don't write any SQL here. By extending JpaRepository<User, Long>,
 * Spring Data JPA automatically generates a working implementation at
 * startup that gives us save(), findById(), findAll(), deleteById(), etc.
 *
 * For the two custom lookups below, Spring Data JPA reads the METHOD NAME
 * and generates the correct SQL just from that — this is called a
 * "derived query method". No @Query annotation or SQL string needed.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Generates: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Generates: SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);

    // Generates: SELECT COUNT(*) FROM users WHERE role = ? (Module 7: admin stats)
    long countByRole(Role role);

    // Generates: SELECT * FROM users ORDER BY created_at DESC (Module 7: admin overview)
    List<User> findAllByOrderByCreatedAtDesc();
}
