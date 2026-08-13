package com.track.track.service;

import com.track.track.entity.User;
import com.track.track.model.Student;
import com.track.track.repository.StudentRepository;
import com.track.track.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository repo;

    @Autowired
    private StudentRepository studentRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    // Add this method to UserService.java
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }

    // =====================================================
    // REGISTER
    // Accept an already-built Student (or null for Advisor)
    // so the controller does NOT also call studentService.save()
    // — fixing the duplicate student record bug.
    // =====================================================

    public User register(User user, Student student) {

        Optional<User> existing =
                repo.findByEmail(
                        user.getEmail()
                );

        if (existing.isPresent()) {
            return null;
        }

        // Hash password before saving

        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );

        User savedUser = repo.save(user);

        // Save student profile if provided

        if (student != null) {
            studentRepository.save(student);
        }

        return savedUser;
    }

    // =====================================================
    // LOGIN
    // =====================================================

    public User login(
            String email,
            String password
    ) {

        Optional<User> user =
                repo.findByEmail(email);

        if (
                user.isPresent()
                        &&
                        passwordEncoder.matches(
                                password,
                                user.get().getPassword()
                        )
        ) {

            return user.get();
        }

        return null;
    }
}