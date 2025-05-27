package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.GradeAssigment;
import co.edu.udes.FrontWeb.model.Period;
import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named("periodController")
@SessionScoped
public class PeriodController implements Serializable {
    @Inject
    private HttpClientService httpClientService;

    private Period currentPeriodGrade;
    private static final String API_URL = "http://localhost:8080/api/periods";

    public void assignGradeToStudent(Long periodId, Long subjectId, Double value) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            GradeAssigment request = new GradeAssigment(periodId, subjectId, value);

            // Llama al backend
            currentPeriodGrade = (Period) httpClientService.post(
                    API_URL + "/grades",
                    request,
                    true
            );

            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Nota asignada correctamente. Nota final del periodo: " + currentPeriodGrade.getFinalGrade(),
                            null));

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error al asignar la nota: " + e.getMessage(),
                            null));
        }
    }
}
