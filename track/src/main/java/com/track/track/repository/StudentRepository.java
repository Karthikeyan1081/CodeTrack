package com.track.track.repository;

import com.track.track.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository
        extends JpaRepository<Student, Long> {

    Student findByEmail(String email);

    Student findByRegisterNumber(String registerNumber);

    // NEW — for advisor department filter

    List<Student> findByDepartment(String department);
}