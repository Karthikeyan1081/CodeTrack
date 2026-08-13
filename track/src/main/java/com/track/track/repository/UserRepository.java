package com.track.track.repository;

import com.track.track.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository
        extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByRegisterNumber(
            String registerNumber
    );
    List<User> findByDepartmentAndRole(String department, String role);
}