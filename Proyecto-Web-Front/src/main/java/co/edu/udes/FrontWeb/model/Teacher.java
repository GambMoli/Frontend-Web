package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    private Long id;          // Opcional, si tu API lo devuelve
    private String name;
    private String email;
    private String password;
    private Integer workloadHours;
}
