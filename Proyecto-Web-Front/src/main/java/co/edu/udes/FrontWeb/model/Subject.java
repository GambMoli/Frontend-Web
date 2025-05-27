package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Subject {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private Double grade;
    private Boolean approved;
    private String noteStudent;
}
