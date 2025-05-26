package co.edu.udes.FrontWeb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Reserve {
    private Long id;
    private String code;
    private LocalDate reserveDate;
    private LocalTime hourInit;
    private LocalTime hourFinish;
    private String state;
    private Place place;
    private Student student;
    private Teacher teacher;

}
