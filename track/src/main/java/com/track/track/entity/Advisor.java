package com.track.track.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "advisors")

public class Advisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BASIC DETAILS

    private String name;

    @Column(unique = true)
    private String email;

    private String department;

    private String section;

    // LOGIN DETAILS

    @Column(unique = true)
    private String username;

    private String password;

    // OPTIONAL

    private String designation;

    private String phoneNumber;
}