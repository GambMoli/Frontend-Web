package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student {
    private Long id;         // Opcional, si tu API lo devuelve
    private String code;
    private String name;
    private String email;
    private String password;
}
