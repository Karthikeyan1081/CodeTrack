package com.track.track.controller;

import com.track.track.model.Student;
import com.track.track.service.ExcelExportService;
import com.track.track.service.RankService;
import com.track.track.service.StatsRefreshService;
import com.track.track.service.StudentService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/students")
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @Autowired
    private ExcelExportService excelService;

    @Autowired
    private StatsRefreshService refreshService;

    @Autowired
    private RankService rankService;

    // ── CRUD ──

    @PostMapping("/add")
    public Student addStudent(
            @RequestBody Student student
    ) {
        return studentService.saveStudent(student);
    }

    @GetMapping("/all")
    public List<Student> getAllStudents() {
        return studentService.getAllStudents();
    }

    @GetMapping("/department/{department}")
    public List<Student> getByDepartment(
            @PathVariable String department
    ) {
        return studentService.getByDepartment(department);
    }

    @GetMapping("/{id}")
    public Student getStudentById(
            @PathVariable Long id
    ) {
        return studentService.getStudentById(id);
    }

    @GetMapping("/register/{registerNumber}")
    public Student getByRegisterNumber(
            @PathVariable String registerNumber
    ) {
        return studentService
                .getByRegisterNumber(registerNumber);
    }

    @PutMapping("/update/{id}")
    public Student updateStudent(
            @PathVariable Long id,
            @RequestBody Student updatedStudent
    ) {
        Student saved = studentService.updateStudent(
                id, updatedStudent
        );
        if (saved != null && hasAnyUsername(saved)) {
            studentService.triggerStatsRefresh(saved.getId());
        }
        return saved;
    }

    @DeleteMapping("/delete/{id}")
    public String deleteStudent(
            @PathVariable Long id
    ) {
        studentService.deleteStudent(id);
        return "Student Deleted Successfully";
    }

    // ── STATS REFRESH ──

    @PostMapping("/refresh/{id}")
    public ResponseEntity<?> refreshStudent(
            @PathVariable Long id
    ) {
        Student updated =
                refreshService.refreshStudent(id);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        rankService.recalculateAllRanks();
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/refresh/all")
    public ResponseEntity<?> refreshAll() {
        refreshService.refreshAll();
        rankService.recalculateAllRanks();
        return ResponseEntity.ok(
                Map.of("message",
                        "All students refreshed successfully")
        );
    }

    @PostMapping("/refresh/department/{department}")
    public ResponseEntity<?> refreshDepartment(
            @PathVariable String department
    ) {
        refreshService.refreshDepartment(department);
        rankService.recalculateDeptRanks(department);
        return ResponseEntity.ok(
                Map.of("message",
                        department + " students refreshed")
        );
    }

    @PostMapping("/recalculate-ranks")
    public ResponseEntity<?> recalculateRanks() {
        rankService.recalculateAllRanks();
        return ResponseEntity.ok(
                Map.of("message",
                        "Ranks recalculated successfully")
        );
    }

    // ── EXPORT ──

    private static final String EXCEL_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportExcel() {
        List<Student> students =
                studentService.getAllStudents();
        ByteArrayInputStream file =
                excelService.exportStudents(students);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=students.xlsx")
                .header(HttpHeaders.CONTENT_TYPE,
                        EXCEL_CONTENT_TYPE)
                .body(new InputStreamResource(file));
    }

    @GetMapping("/export/department/{department}")
    public ResponseEntity<InputStreamResource> exportByDepartment(
            @PathVariable String department
    ) {
        List<Student> students =
                studentService.getByDepartment(department);
        ByteArrayInputStream file =
                excelService.exportStudents(students);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=students_"
                                + department + ".xlsx")
                .header(HttpHeaders.CONTENT_TYPE,
                        EXCEL_CONTENT_TYPE)
                .body(new InputStreamResource(file));
    }

    @GetMapping("/export/custom")
    public ResponseEntity<InputStreamResource> exportCustom(
            @RequestParam List<String> columns
    ) {
        List<Student> students =
                studentService.getAllStudents();
        ByteArrayInputStream file =
                excelService.exportCustomStudents(
                        students, columns
                );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=custom_students.xlsx")
                .header(HttpHeaders.CONTENT_TYPE,
                        EXCEL_CONTENT_TYPE)
                .body(new InputStreamResource(file));
    }

    @GetMapping("/export/custom/department/{department}")
    public ResponseEntity<InputStreamResource> exportCustomByDept(
            @PathVariable String department,
            @RequestParam List<String> columns
    ) {
        List<Student> students =
                studentService.getByDepartment(department);
        ByteArrayInputStream file =
                excelService.exportCustomStudents(
                        students, columns
                );
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=custom_"
                                + department + ".xlsx")
                .header(HttpHeaders.CONTENT_TYPE,
                        EXCEL_CONTENT_TYPE)
                .body(new InputStreamResource(file));
    }

    // ── HELPER ──

    private boolean hasAnyUsername(Student s) {
        return (s.getLeetcodeUsername() != null &&
                !s.getLeetcodeUsername().isBlank())
                || (s.getCodechefUsername() != null &&
                !s.getCodechefUsername().isBlank())
                || (s.getCodeforcesUsername() != null &&
                !s.getCodeforcesUsername().isBlank());
    }
}