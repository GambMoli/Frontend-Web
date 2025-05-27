package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private Long id;
    private String day;
    private String startTime;
    private String endTime;
    private String classroom;
}
