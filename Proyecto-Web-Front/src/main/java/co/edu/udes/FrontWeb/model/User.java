package co.edu.udes.FrontWeb.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String email;
    private String password;
    private String role;

    // Constructor con solo nombre para mantener compatibilidad
    public User(String name) {
        this.name = name;
    }
}