package com.track.track.service;

import com.track.track.entity.Advisor;
import com.track.track.repository.AdvisorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

@Service
public class AdvisorService {

    @Autowired
    private AdvisorRepository advisorRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    // ---------------- GET ALL ----------------

    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAll();
    }

    // ---------------- GET BY ID ----------------

    public Advisor getById(Long id) {
        return advisorRepository
                .findById(id)
                .orElse(null);
    }

    // ---------------- GET BY DEPARTMENT ----------------

    public List<Advisor> getByDepartment(
            String department
    ) {
        return advisorRepository
                .findAllByDepartment(department);
    }

    // ---------------- ADD ----------------

    public Advisor addAdvisor(Advisor advisor) {

        Optional<Advisor> existing =
                advisorRepository
                        .findByEmail(advisor.getEmail());

        if (existing.isPresent()) {
            return null;
        }

        advisor.setPassword(
                passwordEncoder.encode(advisor.getPassword())
        );

        return advisorRepository.save(advisor);
    }

    // ---------------- UPDATE ----------------

    public Advisor updateAdvisor(
            Long id,
            Advisor updatedAdvisor
    ) {

        Advisor advisor =
                advisorRepository
                        .findById(id)
                        .orElse(null);

        if (advisor != null) {

            advisor.setName(
                    updatedAdvisor.getName());

            advisor.setEmail(
                    updatedAdvisor.getEmail());

            advisor.setDepartment(
                    updatedAdvisor.getDepartment());

            advisor.setSection(
                    updatedAdvisor.getSection());

            advisor.setDesignation(
                    updatedAdvisor.getDesignation());

            advisor.setPhoneNumber(
                    updatedAdvisor.getPhoneNumber());

            return advisorRepository.save(advisor);
        }

        return null;
    }

    // ---------------- DELETE ----------------

    public void deleteAdvisor(Long id) {
        advisorRepository.deleteById(id);
    }
}