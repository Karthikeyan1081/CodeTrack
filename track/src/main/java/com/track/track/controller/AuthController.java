package com.track.track.controller;

import com.track.track.dto.AuthResponse;
import com.track.track.entity.Advisor;
import com.track.track.entity.User;
import com.track.track.model.Student;
import com.track.track.security.JwtUtil;
import com.track.track.service.AdvisorService;
import com.track.track.service.StudentService;
import com.track.track.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/auth")
public class AuthController {

    @Autowired private UserService service;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private AdvisorService advisorService;
    @Autowired
    private StudentService studentService;

    // ══════════════════════════════════════
    // LOGIN — only endpoint open to public
    // ══════════════════════════════════════
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody User user) {

        User loggedIn = service.login(
                user.getEmail(), user.getPassword());

        if (loggedIn == null) {
            return ResponseEntity.badRequest()
                    .body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(
                loggedIn.getEmail());

        return ResponseEntity.ok(new AuthResponse(
                token,
                loggedIn.getRole(),
                loggedIn.getEmail(),
                loggedIn.getRegisterNumber(),
                loggedIn.getAdvisorId(),
                loggedIn.getDepartment()
        ));
    }

    // ══════════════════════════════════════
    // ADMIN creates Advisor account
    // POST /auth/admin/create-advisor
    // ══════════════════════════════════════
    @PostMapping("/admin/create-advisor")
    public ResponseEntity<?> createAdvisor(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {

        // Verify caller is ADMIN
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        User caller  = service.findByEmail(email);

        if (caller == null ||
                !"ADMIN".equals(caller.getRole())) {
            return ResponseEntity.status(403)
                    .body("Access denied — Admin only");
        }

        // Check required fields
        if (body.get("email")    == null ||
                body.get("password") == null ||
                body.get("name")     == null ||
                body.get("department") == null) {
            return ResponseEntity.badRequest()
                    .body("Name, email, password and department required");
        }

        // Create User login record
        User user = new User();
        user.setName(body.get("name"));
        user.setEmail(body.get("email"));
        user.setPassword(body.get("password"));
        user.setRole("ADVISOR");
        user.setDepartment(body.get("department"));
        user.setSection(body.get("section"));
        user.setAdvisorId(body.get("email")); // use email as advisorId

        User saved = service.register(user, null);
        if (saved == null) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        // Create Advisor profile record
        Advisor advisor = new Advisor();
        advisor.setName(body.get("name"));
        advisor.setEmail(body.get("email"));
        advisor.setDepartment(body.get("department"));
        advisor.setSection(body.get("section"));
        advisor.setDesignation(body.get("designation"));
        advisor.setPhoneNumber(body.get("phoneNumber"));
        advisor.setUsername(body.get("email"));
        advisorService.addAdvisor(advisor);

        return ResponseEntity.ok(
                "Advisor account created successfully");
    }

    // ══════════════════════════════════════
    // ADVISOR creates Student account
    // POST /auth/advisor/create-student
    // ══════════════════════════════════════
    @PostMapping("/advisor/create-student")
    public ResponseEntity<?> createStudent(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {

        // Verify caller is ADVISOR or ADMIN
        String token  = authHeader.replace("Bearer ", "");
        String email  = jwtUtil.extractUsername(token);
        User caller   = service.findByEmail(email);

        if (caller == null ||
                (!"ADVISOR".equals(caller.getRole()) &&
                        !"ADMIN".equals(caller.getRole()))) {
            return ResponseEntity.status(403)
                    .body("Access denied — Advisor only");
        }

        // Check required fields
        if (body.get("email")          == null ||
                body.get("password")       == null ||
                body.get("name")           == null ||
                body.get("registerNumber") == null) {
            return ResponseEntity.badRequest()
                    .body("Name, email, register number and password required");
        }

        // Create User login record
        User user = new User();
        user.setName(body.get("name"));
        user.setEmail(body.get("email"));
        user.setPassword(body.get("password"));
        user.setRole("STUDENT");
        user.setRegisterNumber(body.get("registerNumber"));
        user.setDepartment(body.get("department"));
        user.setSection(body.get("section"));
        user.setYear(body.get("year"));

        // Create Student profile record
        Student student = new Student();
        student.setName(body.get("name"));
        student.setRegisterNumber(body.get("registerNumber"));
        student.setEmail(body.get("email"));
        student.setDepartment(body.get("department"));
        student.setSection(body.get("section"));
        student.setYear(body.get("year"));
        student.setBatch(body.get("batch"));
        student.setPhoneNumber(body.get("phoneNumber"));

        User saved = service.register(user, null);
        if (saved == null) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        studentService.saveStudent(student);

        return ResponseEntity.ok(
                "Student account created successfully");
    }

    // ══════════════════════════════════════
    // ADMIN creates another ADMIN (optional)
    // POST /auth/admin/create-admin
    // ══════════════════════════════════════
    @PostMapping("/admin/create-admin")
    public ResponseEntity<?> createAdmin(
            @RequestBody Map<String, String> body,
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);
        User caller  = service.findByEmail(email);

        if (caller == null ||
                !"ADMIN".equals(caller.getRole())) {
            return ResponseEntity.status(403)
                    .body("Access denied — Admin only");
        }

        User user = new User();
        user.setName(body.get("name"));
        user.setEmail(body.get("email"));
        user.setPassword(body.get("password"));
        user.setRole("ADMIN");

        // Check email not already taken
        User saved = service.register(user, null);
        if (saved == null) {
            return ResponseEntity.badRequest()
                    .body("Email already exists");
        }

        // Save student via StudentService so async stats
        // fetch + rank calculation triggers automatically

        return ResponseEntity.ok(
                "Student account created successfully");
    }
}