package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GradeAssigment {
    private Long periodId;
    private Long subjectId;
    private Double value;
}
