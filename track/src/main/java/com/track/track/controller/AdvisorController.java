package com.track.track.controller;

import com.track.track.entity.Advisor;
import com.track.track.service.AdvisorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advisors")
@CrossOrigin(origins = "*")
public class AdvisorController {

    @Autowired
    private AdvisorService advisorService;

    // ---------------- GET ALL ----------------

    @GetMapping("/all")
    public List<Advisor> getAllAdvisors() {
        return advisorService.getAllAdvisors();
    }

    // ---------------- GET BY ID ----------------

    @GetMapping("/{id}")
    public Advisor getById(@PathVariable Long id) {
        return advisorService.getById(id);
    }

    // ---------------- GET BY DEPARTMENT ----------------

    @GetMapping("/department/{department}")
    public List<Advisor> getByDepartment(
            @PathVariable String department
    ) {
        return advisorService.getByDepartment(department);
    }

    // ---------------- ADD ----------------

    @PostMapping("/add")
    public ResponseEntity<?> addAdvisor(
            @RequestBody Advisor advisor
    ) {
        Advisor saved =
                advisorService.addAdvisor(advisor);

        if (saved == null) {
            return ResponseEntity
                    .badRequest()
                    .body("Email already exists");
        }

        return ResponseEntity.ok(saved);
    }

    // ---------------- UPDATE ----------------

    @PutMapping("/update/{id}")
    public Advisor updateAdvisor(
            @PathVariable Long id,
            @RequestBody Advisor updatedAdvisor
    ) {
        return advisorService.updateAdvisor(
                id, updatedAdvisor
        );
    }

    // ---------------- DELETE ----------------

    @DeleteMapping("/delete/{id}")
    public String deleteAdvisor(
            @PathVariable Long id
    ) {
        advisorService.deleteAdvisor(id);
        return "Advisor Deleted Successfully";
    }
}