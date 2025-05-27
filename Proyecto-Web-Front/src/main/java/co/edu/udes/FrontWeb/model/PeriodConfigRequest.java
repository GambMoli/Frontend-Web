package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeriodConfigRequest {
    private Long studentId;
    private List<PeriodConfiguration> periodConfigurations;
}