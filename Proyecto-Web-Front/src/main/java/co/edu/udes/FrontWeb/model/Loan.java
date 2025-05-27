package co.edu.udes.FrontWeb.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor

@JsonIgnoreProperties(ignoreUnknown = true)
public class Loan {
    private Long id;
    private String code;                  // Código del préstamo, ej: "LOAN-2023-001"
    private String     loanDate;
    private String     deadline;
    private LocalDate   actualReturnDate;
    private Long materialId;// ID del material reservado
    private Material material;
    private Long studentId;              // ID del estudiante (nullable si es un docente)
    private Long teacherId;              // ID del profesor (nullable si es un estudiante)

    private String status;               // Estado del préstamo (ej. "ACTIVE", "RETURNED", etc.)
    private String returnState;
}
