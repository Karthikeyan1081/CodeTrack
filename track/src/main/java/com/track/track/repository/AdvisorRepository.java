package com.track.track.repository;

import com.track.track.entity.Advisor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdvisorRepository
        extends JpaRepository<Advisor, Long> {

    Optional<Advisor> findByDepartment(String department);

    Optional<Advisor> findByUsername(String username);

    Optional<Advisor> findByEmail(String email);

    // NEW — for admin department filter

    List<Advisor> findAllByDepartment(String department);
}