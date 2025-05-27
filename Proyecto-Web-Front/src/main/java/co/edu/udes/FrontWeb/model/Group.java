package co.edu.udes.FrontWeb.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    private Long id;
    private String name;
    private int capacity;
    private int enrolledCount;
    private Long subjectId;
    private String subjectName;
    private List<Student> students;
    private List<Schedule> schedules;
}
