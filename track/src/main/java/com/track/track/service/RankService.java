package com.track.track.service;

import com.track.track.model.Student;
import com.track.track.repository.StudentRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RankService {

    @Autowired
    private StudentRepository studentRepository;

    // =====================================================
    // RECALCULATE ALL RANKS
    // =====================================================

    public void recalculateAllRanks() {

        List<Student> all =
                studentRepository.findAll();

        // ── DEPARTMENT RANKS ──
        Map<String, List<Student>> byDept =
                all.stream()
                        .filter(s -> s.getDepartment() != null)
                        .collect(Collectors.groupingBy(
                                Student::getDepartment
                        ));

        for (List<Student> deptStudents : byDept.values()) {
            deptStudents.sort(
                    Comparator.comparingInt(
                            (Student s) -> safe(s.getTotalSolved())
                    ).reversed()
            );
            assignRanksWithTies(deptStudents, false);
        }

        // ── COLLEGE RANKS ──
        all.sort(
                Comparator.comparingInt(
                        (Student s) -> safe(s.getTotalSolved())
                ).reversed()
        );
        assignRanksWithTies(all, true);

        studentRepository.saveAll(all);
    }

    // =====================================================
    // RECALCULATE RANKS FOR ONE DEPARTMENT
    // =====================================================

    public void recalculateDeptRanks(String department) {

        List<Student> all =
                studentRepository.findAll();

        // Department rank
        List<Student> deptStudents =
                all.stream()
                        .filter(s -> department.equals(
                                s.getDepartment()
                        ))
                        .sorted(Comparator.comparingInt(
                                (Student s) -> safe(s.getTotalSolved())
                        ).reversed())
                        .collect(Collectors.toList());

        assignRanksWithTies(deptStudents, false);

        // College rank
        all.sort(
                Comparator.comparingInt(
                        (Student s) -> safe(s.getTotalSolved())
                ).reversed()
        );
        assignRanksWithTies(all, true);

        studentRepository.saveAll(all);
    }

    // =====================================================
    // ASSIGN RANKS WITH TIE HANDLING
    // isCollege = true  → sets collegeRank
    // isCollege = false → sets departmentRank
    // =====================================================

    private void assignRanksWithTies(
            List<Student> students,
            boolean isCollege
    ) {
        int rank = 1;

        for (int i = 0; i < students.size(); i++) {

            // If not first and same score as previous → same rank
            if (i > 0 &&
                    safe(students.get(i).getTotalSolved()) ==
                            safe(students.get(i - 1).getTotalSolved())) {

                // Same score — assign same rank as previous
                if (isCollege) {
                    students.get(i).setCollegeRank(
                            students.get(i - 1).getCollegeRank()
                    );
                } else {
                    students.get(i).setDepartmentRank(
                            students.get(i - 1).getDepartmentRank()
                    );
                }

            } else {
                // Different score — assign current position
                rank = i + 1;
                if (isCollege) {
                    students.get(i).setCollegeRank(rank);
                } else {
                    students.get(i).setDepartmentRank(rank);
                }
            }
        }
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}