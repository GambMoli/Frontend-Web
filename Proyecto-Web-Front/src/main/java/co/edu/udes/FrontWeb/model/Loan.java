package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Loan {
    private Long id;
    private String code;                  // Código del préstamo, ej: "LOAN-2023-001"
    private LocalDateTime loanDate;      // Fecha y hora del préstamo
    private LocalDateTime actualReturnDate; // Fecha de devolución real (null al inicio)

    private Material materialId;             // ID del material reservado
    private Long studentId;              // ID del estudiante (nullable si es un docente)
    private Long teacherId;              // ID del profesor (nullable si es un estudiante)

    private String status;               // Estado del préstamo (ej. "ACTIVE", "RETURNED", etc.)
    private String returnState;
}
