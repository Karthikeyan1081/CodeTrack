package com.track.track.dto;

public class AuthResponse {

    private String token;
    private String role;
    private String email;
    private String registerNumber;
    private String advisorId;
    private String department;

    public AuthResponse() {
    }

    public AuthResponse(
            String token,
            String role,
            String email,
            String registerNumber,
            String advisorId,
            String department
    ) {
        this.token = token;
        this.role = role;
        this.email = email;
        this.registerNumber = registerNumber;
        this.department = department;
        this.advisorId=advisorId;
    }

    // ================= GETTERS & SETTERS =================

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getAdvisorId() {
        return advisorId;
    }

    public void setAdvisorId(String advisorId) {
        this.advisorId = advisorId;
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}