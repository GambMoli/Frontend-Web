package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ScheduleController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private LoginController loginController;

    private Map<String, Object> scheduleData;
    private List<Map<String, Object>> weekSchedule;
    private String studentName;
    private boolean loading = true;
    private boolean error = false;
    private String errorMessage;

    @PostConstruct
    public void init() {
        loadSchedule();
    }

    private void loadSchedule() {
        try {
            if (loginController == null || loginController.getId() == null) {
                error = true;
                errorMessage = "No se pudo obtener la información de sesión";
                loading = false;
                return;
            }

            // Obtener el horario del estudiante
            scheduleData = (Map<String, Object>) httpClientService.get(
                    "http://localhost:8080/api/students/" + loginController.getId() + "/schedule",
                    true
            );

            if (scheduleData == null) {
                error = true;
                errorMessage = "No se encontró información del horario";
                loading = false;
                return;
            }

            // Extraer información del response
            studentName = (String) scheduleData.get("studentName");
            weekSchedule = (List<Map<String, Object>>) scheduleData.get("weekSchedule");

            if (weekSchedule == null) {
                weekSchedule = new ArrayList<>();
            }

        } catch (Exception e) {
            e.printStackTrace();
            error = true;
            errorMessage = "Error al cargar el horario: " + e.getMessage();
        } finally {
            loading = false;
        }
    }

    // Métodos para la vista
    public List<String> getDayNames() {
        if (weekSchedule == null) return new ArrayList<>();
        return weekSchedule.stream()
                .map(day -> (String) day.get("dayName"))
                .collect(Collectors.toList());
    }

    private List<String> timeSlots = Arrays.asList(
            "7:00 - 8:00", "8:00 - 9:00", "9:00 - 10:00",
            "10:00 - 11:00", "11:00 - 12:00", "12:00 - 13:00",
            "13:00 - 14:00", "14:00 - 15:00", "15:00 - 16:00",
            "16:00 - 17:00", "17:00 - 18:00", "18:00 - 19:00"
    );

    public List<String> getTimeSlots() {
        return timeSlots;
    }

    public List<Map<String, Object>> getClassesForDayAndTime(String dayName, String timeSlot) {
        if (weekSchedule == null) return new ArrayList<>();

        List<Map<String, Object>> dayClasses = weekSchedule.stream()
                .filter(day -> dayName.equals(day.get("dayName")))
                .findFirst()
                .map(day -> (List<Map<String, Object>>) day.get("classes"))
                .orElse(new ArrayList<>());

        if (dayClasses.isEmpty()) return new ArrayList<>();

        String[] slotParts = timeSlot.split(" - ");
        String slotStart = slotParts[0];
        String slotEnd = slotParts[1];

        slotStart = slotStart.length() == 4 ? "0" + slotStart : slotStart;
        slotEnd = slotEnd.length() == 4 ? "0" + slotEnd : slotEnd;

        String finalSlotStart = slotStart;
        String finalSlotEnd = slotEnd;
        return dayClasses.stream()
                .filter(c -> {
                    String classStart = c.get("startTime").toString();
                    String classEnd = c.get("endTime").toString();

                    // Verificar si el slot está completamente dentro de la clase
                    return (classStart.compareTo(finalSlotStart) <= 0 &&
                            classEnd.compareTo(finalSlotEnd) >= 0);
                })
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> getWeekSchedule() {
        return weekSchedule != null ? weekSchedule : new ArrayList<>();
    }

    public String getStudentName() {
        return studentName;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void refresh() {
        loading = true;
        error = false;
        errorMessage = null;
        loadSchedule();
    }

    // Método para obtener el JSON del horario (opcional)
    public String getWeekScheduleJson() {
        try {
            if (weekSchedule == null) return "[]";

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < weekSchedule.size(); i++) {
                Map<String, Object> day = weekSchedule.get(i);
                if (i > 0) json.append(",");

                json.append("{");
                json.append("\"dayName\":\"").append(day.get("dayName")).append("\",");
                json.append("\"classes\":[");

                List<Map<String, Object>> classes = (List<Map<String, Object>>) day.get("classes");
                if (classes != null) {
                    for (int j = 0; j < classes.size(); j++) {
                        Map<String, Object> classItem = classes.get(j);
                        if (j > 0) json.append(",");

                        json.append("{");
                        json.append("\"subjectName\":\"").append(classItem.get("subjectName")).append("\",");
                        json.append("\"startTime\":\"").append(classItem.get("startTime")).append("\",");
                        json.append("\"endTime\":\"").append(classItem.get("endTime")).append("\",");
                        json.append("\"classroom\":\"").append(classItem.get("classroom")).append("\"");
                        json.append("}");
                    }
                }

                json.append("]}");
            }
            json.append("]");

            return json.toString();
        } catch (Exception e) {
            return "[]";
        }
    }
}