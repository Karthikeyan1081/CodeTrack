package com.track.track.service;

import com.track.track.model.Student;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ExcelExportService {

    // ======================================================
    // MAIN EXPORT — 3 sheets matching reference format
    // ======================================================

    public ByteArrayInputStream exportStudents(
            List<Student> students
    ) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            CellStyle title  = makeTitleStyle(wb);
            CellStyle header = makeHeaderStyle(wb);
            CellStyle data   = makeDataStyle(wb);
            CellStyle alt    = makeAltStyle(wb);

            buildLeetCodeSheet(
                    wb, students, title, header, data, alt
            );
            buildCodeChefSheet(
                    wb, students, title, header, data, alt
            );
            buildCodeforcesSheet(
                    wb, students, title, header, data, alt
            );

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();
            wb.write(out);
            return new ByteArrayInputStream(
                    out.toByteArray()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Excel Export Failed: " + e.getMessage()
            );
        }
    }

    // ======================================================
    // SHEET 1 — LEETCODE
    // Columns: S.No | Name | Username | Easy | Medium |
    //          Hard | Total | Contests | Rating | GlobalRank
    //          | Top% | Last Active
    // ======================================================

    private void buildLeetCodeSheet(
            XSSFWorkbook wb,
            List<Student> students,
            CellStyle title, CellStyle header,
            CellStyle data, CellStyle alt
    ) {
        Sheet sheet = wb.createSheet("LEETCODE");

        // Title
        Row tr = sheet.createRow(0);
        tr.setHeightInPoints(24);
        Cell tc = tr.createCell(0);
        tc.setCellValue(
                "LEETCODE TRACKER — " +
                        students.size() + " Students"
        );
        tc.setCellStyle(title);
        sheet.addMergedRegion(
                new CellRangeAddress(0, 0, 0, 11)
        );

        // Headers
        String[] headers = {
                "S.No", "Name of the Student",
                "LeetCode Username",
                "Easy Solved", "Medium Solved", "Hard Solved",
                "Total Solved", "Contest Count",
                "Contest Rating", "Global Rank",
                "Top %", "Last Active"
        };
        Row hRow = sheet.createRow(1);
        hRow.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        // Data
        int rowNum = 2;
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            Row row   = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            CellStyle st = (i % 2 == 0) ? data : alt;

            cell(row, 0,  String.valueOf(i + 1), st);
            cell(row, 1,  safe(s.getName()), st);
            cell(row, 2,  safe(s.getLeetcodeUsername()), st);
            cell(row, 3,  safeInt(s.getLeetcodeEasySolved()), st);
            cell(row, 4,  safeInt(s.getLeetcodeMediumSolved()), st);
            cell(row, 5,  safeInt(s.getLeetcodeHardSolved()), st);
            cell(row, 6,  safeInt(s.getLeetcodeTotalSolved()), st);
            cell(row, 7,  safeInt(s.getLeetcodeContestCount()), st);
            cell(row, 8,  safeInt(s.getLeetcodeContestRating()), st);
            cell(row, 9,  safe(s.getLeetcodeGlobalRank()), st);
            cell(row, 10, safe(s.getLeetcodeTopPercentage()), st);
            cell(row, 11, safe(s.getLeetcodeLastActive()), st);
        }

        summaryRow(sheet, rowNum, students.size(),
                title, 11);

        int[] w = {
                6, 28, 20, 12, 14, 12,
                12, 14, 14, 14, 10, 14
        };
        setWidths(sheet, w);
        sheet.createFreezePane(0, 2);
    }

    // ======================================================
    // SHEET 2 — CODECHEF
    // Columns: S.No | Name | Username | CurrentRating |
    //          HighestRating | Division | Stars |
    //          GlobalRank | CountryRank | Contests |
    //          ProblemsSolved | Last Active
    // ======================================================

    private void buildCodeChefSheet(
            XSSFWorkbook wb,
            List<Student> students,
            CellStyle title, CellStyle header,
            CellStyle data, CellStyle alt
    ) {
        Sheet sheet = wb.createSheet("CODE-CHEF");

        Row tr = sheet.createRow(0);
        tr.setHeightInPoints(24);
        Cell tc = tr.createCell(0);
        tc.setCellValue(
                "CODE CHEF TRACKER — " +
                        students.size() + " Students"
        );
        tc.setCellStyle(title);
        sheet.addMergedRegion(
                new CellRangeAddress(0, 0, 0, 11)
        );

        String[] headers = {
                "S.No", "Name of the Student",
                "CodeChef Username",
                "Current Rating", "Highest Rating",
                "Division", "Stars",
                "Global Ranking", "Country Ranking",
                "Contests Participated",
                "Problems Solved", "Last Active"
        };
        Row hRow = sheet.createRow(1);
        hRow.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        int rowNum = 2;
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            Row row   = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            CellStyle st = (i % 2 == 0) ? data : alt;

            cell(row, 0,  String.valueOf(i + 1), st);
            cell(row, 1,  safe(s.getName()), st);
            cell(row, 2,  safe(s.getCodechefUsername()), st);
            cell(row, 3,  safeInt(s.getCodechefCurrentRating()), st);
            cell(row, 4,  safeInt(s.getCodechefHighestRating()), st);
            cell(row, 5,  safe(s.getCodechefDivision()), st);
            cell(row, 6,  safe(s.getCodechefStars()), st);
            cell(row, 7,  safe(s.getCodechefGlobalRank()), st);
            cell(row, 8,  safe(s.getCodechefCountryRank()), st);
            cell(row, 9,  safeInt(s.getCodechefContests()), st);
            cell(row, 10, safeInt(s.getCodechefProblemsSolved()), st);
            cell(row, 11, safe(s.getCodechefLastActive()), st);
        }

        summaryRow(sheet, rowNum, students.size(),
                title, 11);

        int[] w = {
                6, 28, 20, 14, 14,
                12, 10, 16, 16, 22, 16, 14
        };
        setWidths(sheet, w);
        sheet.createFreezePane(0, 2);
    }

    // ======================================================
    // SHEET 3 — CODEFORCES
    // Columns: S.No | Name | Username | CurrentRating |
    //          MaxRating | CurrentRank | MaxRank |
    //          ProblemsCount | Contests | Level | Last Active
    // ======================================================

    private void buildCodeforcesSheet(
            XSSFWorkbook wb,
            List<Student> students,
            CellStyle title, CellStyle header,
            CellStyle data, CellStyle alt
    ) {
        Sheet sheet = wb.createSheet("CODE FORCE");

        Row tr = sheet.createRow(0);
        tr.setHeightInPoints(24);
        Cell tc = tr.createCell(0);
        tc.setCellValue(
                "CODE FORCE TRACKER — " +
                        students.size() + " Students"
        );
        tc.setCellStyle(title);
        sheet.addMergedRegion(
                new CellRangeAddress(0, 0, 0, 10)
        );

        String[] headers = {
                "S.No", "Name of the Student",
                "Codeforces Username",
                "Current Rating", "Max Rating",
                "Current Ranking", "Max Rank",
                "Problems Count", "Contests Attended",
                "Level", "Last Active"
        };
        Row hRow = sheet.createRow(1);
        hRow.setHeightInPoints(20);
        for (int i = 0; i < headers.length; i++) {
            Cell c = hRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(header);
        }

        int rowNum = 2;
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            Row row   = sheet.createRow(rowNum++);
            row.setHeightInPoints(18);
            CellStyle st = (i % 2 == 0) ? data : alt;

            cell(row, 0,  String.valueOf(i + 1), st);
            cell(row, 1,  safe(s.getName()), st);
            cell(row, 2,  safe(s.getCodeforcesUsername()), st);
            cell(row, 3,  safeInt(s.getCodeforcesCurrentRating()), st);
            cell(row, 4,  safeInt(s.getCodeforcesMaxRating()), st);
            cell(row, 5,  safe(s.getCodeforcesCurrentRank()), st);
            cell(row, 6,  safe(s.getCodeforcesMaxRank()), st);
            cell(row, 7,  safeInt(s.getCodeforcesProblemsCount()), st);
            cell(row, 8,  safeInt(s.getCodeforcesContests()), st);
            cell(row, 9,  safe(s.getCodeforcesLevel()), st);
            cell(row, 10, safe(s.getCodeforcesLastActive()), st);
        }

        summaryRow(sheet, rowNum, students.size(),
                title, 10);

        int[] w = {
                6, 28, 22, 14, 12,
                18, 14, 16, 18, 14, 14
        };
        setWidths(sheet, w);
        sheet.createFreezePane(0, 2);
    }

    // ======================================================
    // CUSTOM EXPORT — advisor picks columns
    // ======================================================

    public ByteArrayInputStream exportCustomStudents(
            List<Student> students,
            List<String> columns
    ) {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {

            Sheet sheet = wb.createSheet("Students");
            CellStyle header = makeHeaderStyle(wb);
            CellStyle data   = makeDataStyle(wb);
            CellStyle alt    = makeAltStyle(wb);

            Row hRow = sheet.createRow(0);
            hRow.setHeightInPoints(20);
            for (int i = 0; i < columns.size(); i++) {
                Cell c = hRow.createCell(i);
                c.setCellValue(
                        formatColumnName(columns.get(i))
                );
                c.setCellStyle(header);
            }

            int rowNum = 1;
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                Row row   = sheet.createRow(rowNum++);
                row.setHeightInPoints(18);
                CellStyle st = (i % 2 == 0) ? data : alt;

                for (int j = 0;
                     j < columns.size(); j++) {
                    Cell c = row.createCell(j);
                    c.setCellValue(
                            getColumnValue(
                                    s, columns.get(j)
                            )
                    );
                    c.setCellStyle(st);
                }
            }

            for (int i = 0;
                 i < columns.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            sheet.createFreezePane(0, 1);

            ByteArrayOutputStream out =
                    new ByteArrayOutputStream();
            wb.write(out);
            return new ByteArrayInputStream(
                    out.toByteArray()
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Custom Excel Export Failed"
            );
        }
    }

    // ======================================================
    // COLUMN VALUE — custom export
    // ======================================================

    private String getColumnValue(
            Student s, String col
    ) {
        switch (col) {

            // BASIC
            case "name":
                return safe(s.getName());
            case "registerNumber":
                return safe(s.getRegisterNumber());
            case "year":
                return safe(s.getYear());
            case "department":
                return safe(s.getDepartment());
            case "section":
                return safe(s.getSection());
            case "email":
                return safe(s.getEmail());
            case "phoneNumber":
                return safe(s.getPhoneNumber());
            case "linkedinUrl":
                return safe(s.getLinkedinUrl());
            case "portfolioUrl":
                return safe(s.getPortfolioUrl());

            // USERNAMES
            case "leetcodeUsername":
                return safe(s.getLeetcodeUsername());
            case "codechefUsername":
                return safe(s.getCodechefUsername());
            case "codeforcesUsername":
                return safe(s.getCodeforcesUsername());
            case "githubUsername":
                return safe(s.getGithubUsername());
            case "hackerrankUsername":
                return safe(s.getHackerrankUsername());
            case "geeksforgeeksUsername":
                return safe(
                        s.getGeeksforgeeksUsername()
                );

            // LEETCODE
            case "leetcodeEasySolved":
                return safeInt(
                        s.getLeetcodeEasySolved()
                );
            case "leetcodeMediumSolved":
                return safeInt(
                        s.getLeetcodeMediumSolved()
                );
            case "leetcodeHardSolved":
                return safeInt(
                        s.getLeetcodeHardSolved()
                );
            case "leetcodeTotalSolved":
                return safeInt(
                        s.getLeetcodeTotalSolved()
                );
            case "leetcodeContestCount":
                return safeInt(
                        s.getLeetcodeContestCount()
                );
            case "leetcodeContestRating":
                return safeInt(
                        s.getLeetcodeContestRating()
                );
            case "leetcodeGlobalRank":
                return safe(s.getLeetcodeGlobalRank());
            case "leetcodeTopPercentage":
                return safe(
                        s.getLeetcodeTopPercentage()
                );

            // CODECHEF
            case "codechefCurrentRating":
                return safeInt(
                        s.getCodechefCurrentRating()
                );
            case "codechefHighestRating":
                return safeInt(
                        s.getCodechefHighestRating()
                );
            case "codechefDivision":
                return safe(s.getCodechefDivision());
            case "codechefStars":
                return safe(s.getCodechefStars());
            case "codechefGlobalRank":
                return safe(s.getCodechefGlobalRank());
            case "codechefCountryRank":
                return safe(
                        s.getCodechefCountryRank()
                );
            case "codechefContests":
                return safeInt(s.getCodechefContests());
            case "codechefProblemsSolved":
                return safeInt(
                        s.getCodechefProblemsSolved()
                );

            // CODEFORCES
            case "codeforcesCurrentRating":
                return safeInt(
                        s.getCodeforcesCurrentRating()
                );
            case "codeforcesMaxRating":
                return safeInt(
                        s.getCodeforcesMaxRating()
                );
            case "codeforcesCurrentRank":
                return safe(
                        s.getCodeforcesCurrentRank()
                );
            case "codeforcesMaxRank":
                return safe(s.getCodeforcesMaxRank());
            case "codeforcesProblemsCount":
                return safeInt(
                        s.getCodeforcesProblemsCount()
                );
            case "codeforcesContests":
                return safeInt(
                        s.getCodeforcesContests()
                );
            case "codeforcesLevel":
                return safe(s.getCodeforcesLevel());

            // OVERALL
            case "totalSolved":
                return safeInt(s.getTotalSolved());
            case "highestRating":
                return safeInt(s.getHighestRating());
            case "totalContests":
                return safeInt(s.getTotalContests());
            case "departmentRank":
                return safeInt(s.getDepartmentRank());
            case "collegeRank":
                return safeInt(s.getCollegeRank());
            case "lastUpdated":
                return safe(s.getLastUpdated());

            default:
                return "-";
        }
    }

    // ======================================================
    // COLUMN NAME FORMATTER
    // ======================================================

    private String formatColumnName(String col) {
        switch (col) {
            case "registerNumber":
                return "Register Number";
            case "phoneNumber":
                return "Phone Number";
            case "linkedinUrl":
                return "LinkedIn URL";
            case "portfolioUrl":
                return "Portfolio URL";
            case "leetcodeUsername":
                return "LeetCode Username";
            case "leetcodeEasySolved":
                return "LC Easy Solved";
            case "leetcodeMediumSolved":
                return "LC Medium Solved";
            case "leetcodeHardSolved":
                return "LC Hard Solved";
            case "leetcodeTotalSolved":
                return "LC Total Solved";
            case "leetcodeContestCount":
                return "LC Contest Count";
            case "leetcodeContestRating":
                return "LC Contest Rating";
            case "leetcodeGlobalRank":
                return "LC Global Rank";
            case "leetcodeTopPercentage":
                return "LC Top %";
            case "codechefUsername":
                return "CodeChef Username";
            case "codechefCurrentRating":
                return "CC Current Rating";
            case "codechefHighestRating":
                return "CC Highest Rating";
            case "codechefDivision":
                return "CC Division";
            case "codechefStars":
                return "CC Stars";
            case "codechefGlobalRank":
                return "CC Global Rank";
            case "codechefCountryRank":
                return "CC Country Rank";
            case "codechefContests":
                return "CC Contests";
            case "codechefProblemsSolved":
                return "CC Problems Solved";
            case "codeforcesUsername":
                return "Codeforces Username";
            case "codeforcesCurrentRating":
                return "CF Current Rating";
            case "codeforcesMaxRating":
                return "CF Max Rating";
            case "codeforcesCurrentRank":
                return "CF Current Rank";
            case "codeforcesMaxRank":
                return "CF Max Rank";
            case "codeforcesProblemsCount":
                return "CF Problems Count";
            case "codeforcesContests":
                return "CF Contests";
            case "codeforcesLevel":
                return "CF Level";
            case "githubUsername":
                return "GitHub Username";
            case "hackerrankUsername":
                return "HackerRank Username";
            case "geeksforgeeksUsername":
                return "GeeksforGeeks Username";
            case "totalSolved":
                return "Total Solved";
            case "highestRating":
                return "Highest Rating";
            case "totalContests":
                return "Total Contests";
            case "departmentRank":
                return "Department Rank";
            case "collegeRank":
                return "College Rank";
            case "lastUpdated":
                return "Last Updated";
            default:
                return col.substring(0, 1)
                        .toUpperCase()
                        + col.substring(1);
        }
    }

    // ======================================================
    // STYLES
    // ======================================================

    private CellStyle makeTitleStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f  = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 13);
        f.setFontName("Arial");
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)15,(byte)76,(byte)129},
                null
        ));
        s.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle makeHeaderStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f  = wb.createFont();
        f.setBold(true);
        f.setFontHeightInPoints((short) 11);
        f.setFontName("Arial");
        f.setColor(IndexedColors.WHITE.getIndex());
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)30,(byte)58,(byte)95},
                null
        ));
        s.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s);
        s.setWrapText(true);
        return s;
    }

    private CellStyle makeDataStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f  = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setFontName("Arial");
        s.setFont(f);
        s.setFillForegroundColor(
                IndexedColors.WHITE.getIndex()
        );
        s.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private CellStyle makeAltStyle(XSSFWorkbook wb) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f  = wb.createFont();
        f.setFontHeightInPoints((short) 10);
        f.setFontName("Arial");
        s.setFont(f);
        s.setFillForegroundColor(new XSSFColor(
                new byte[]{(byte)235,(byte)241,(byte)250},
                null
        ));
        s.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );
        s.setAlignment(HorizontalAlignment.CENTER);
        s.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(s);
        return s;
    }

    private void setBorders(CellStyle s) {
        s.setBorderBottom(BorderStyle.THIN);
        s.setBorderTop(BorderStyle.THIN);
        s.setBorderLeft(BorderStyle.THIN);
        s.setBorderRight(BorderStyle.THIN);
    }

    // ======================================================
    // HELPERS
    // ======================================================

    private void cell(
            Row row, int col,
            String val, CellStyle style
    ) {
        Cell c = row.createCell(col);
        c.setCellValue(val);
        c.setCellStyle(style);
    }

    private void summaryRow(
            Sheet sheet, int rowNum,
            int count, CellStyle style, int lastCol
    ) {
        int r = rowNum + 1;
        Row row = sheet.createRow(r);
        row.setHeightInPoints(20);
        Cell c = row.createCell(0);
        c.setCellValue("Total Students: " + count);
        c.setCellStyle(style);
        sheet.addMergedRegion(
                new CellRangeAddress(r, r, 0, lastCol)
        );
    }

    private void setWidths(Sheet sheet, int[] w) {
        for (int i = 0; i < w.length; i++) {
            sheet.setColumnWidth(i, w[i] * 256);
        }
    }

    private String safe(String v) {
        return (v == null || v.isBlank()) ? "-" : v;
    }

    private String safeInt(Integer v) {
        return (v == null || v == 0)
                ? "-" : String.valueOf(v);
    }
}