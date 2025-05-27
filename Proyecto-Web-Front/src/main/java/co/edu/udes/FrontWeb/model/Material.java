package co.edu.udes.FrontWeb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Material {
    private long id;
    private String name;
    private String code;
    private String description;
    private String type;
    private String state;
    private int stock;
    @JsonIgnore
    private LocalDate entryDate;
}
