package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Period {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double weight;
    private Long studentId;
    private String studentName;
    private List<Subject> subjectGrades;
    private Double finalGrade;
    private Boolean approved;
}
