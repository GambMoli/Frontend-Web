package co.edu.udes.FrontWeb.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitializerPeriods {
    private Long studentId;
    private List<PeriodDateRangeDTO> periodConfigurations;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PeriodDateRangeDTO {
        private String name;
        private Double weight;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
