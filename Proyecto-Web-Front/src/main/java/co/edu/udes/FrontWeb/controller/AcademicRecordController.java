package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("academicRecordController")
@ViewScoped
public class AcademicRecordController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private LoginController loginController;

    private List<Map<String, Object>> currentSemesterGrades;
    private String currentSemester = "2024-1";
    private boolean loading = true;
    private boolean error = false;
    private String errorMessage;

    @PostConstruct
    public void init() {
        loadAcademicRecord();
    }

    private void loadAcademicRecord() {
        try {
            if (loginController == null || loginController.getId() == null) {
                error = true;
                errorMessage = "No se pudo obtener la información de sesión";
                loading = false;
                return;
            }

            // Get academic record to obtain subject list
            Map<String, Object> academicRecord = (Map<String, Object>) httpClientService.get(
                    "http://localhost:8080/api/students/" + loginController.getId() + "/academic-record",
                    true
            );

            if (academicRecord == null) {
                currentSemesterGrades = new ArrayList<>();
                loading = false;
                return;
            }

            // Get all subjects
            List<Map<String, Object>> allSubjects = new ArrayList<>();
            List<Map<String, Object>> completed = (List<Map<String, Object>>)
                    academicRecord.getOrDefault("completedSubjects", new ArrayList<>());
            List<Map<String, Object>> inProgress = (List<Map<String, Object>>)
                    academicRecord.getOrDefault("inProgressSubjects", new ArrayList<>());
            List<Map<String, Object>> pending = (List<Map<String, Object>>)
                    academicRecord.getOrDefault("pendingSubjects", new ArrayList<>());

            allSubjects.addAll(completed);
            allSubjects.addAll(inProgress);
            allSubjects.addAll(pending);

            // Get grades for each subject
            currentSemesterGrades = getGradesForSubjects(allSubjects);

        } catch (Exception e) {
            e.printStackTrace();
            error = true;
            errorMessage = "Error al cargar el record académico: " + e.getMessage();
        } finally {
            loading = false;
        }
    }

    private List<Map<String, Object>> getGradesForSubjects(List<Map<String, Object>> subjects) {
        return subjects.stream()
                .map(this::getSubjectGrades)
                .filter(subject -> subject != null)
                .collect(Collectors.toList());
    }

    private Map<String, Object> getSubjectGrades(Map<String, Object> subject) {
        Map<String, Object> gradeInfo = new HashMap<>();

        // Basic subject info
        gradeInfo.put("subjectId", subject.get("id"));
        gradeInfo.put("subjectName", subject.get("name"));
        gradeInfo.put("credits", subject.get("credits"));

        try {
            String studentId = String.valueOf(loginController.getId());
            String subjectId = String.valueOf(subject.get("id"));

            String endpoint = "http://localhost:8080/api/periods/calculate-subject-final/" +
                    studentId + "/" + subjectId;

            Object response = httpClientService.get(endpoint, true);

            if (response instanceof Map) {
                Map<String, Object> gradeData = (Map<String, Object>) response;
                processGradeData(gradeInfo, gradeData);
            } else if (response instanceof List) {
                List<?> gradeList = (List<?>) response;
                if (!gradeList.isEmpty() && gradeList.get(0) instanceof Map) {
                    Map<String, Object> gradeData = (Map<String, Object>) gradeList.get(0);
                    processGradeData(gradeInfo, gradeData);
                }
            }

        } catch (Exception e) {
            // Set default values if no grades found
            gradeInfo.put("grade1", "-");
            gradeInfo.put("grade2", "-");
            gradeInfo.put("grade3", "-");
            gradeInfo.put("finalGrade", "-");
            gradeInfo.put("approved", false);
        }

        return gradeInfo;
    }

    private void processGradeData(Map<String, Object> gradeInfo, Map<String, Object> gradeData) {
        // Set final grade and approval status
        gradeInfo.put("finalGrade", formatGrade(gradeData.get("finalGrade")));
        gradeInfo.put("approved", gradeData.get("approved"));

        // Process period grades
        List<Map<String, Object>> periodGrades = (List<Map<String, Object>>)
                gradeData.getOrDefault("periodGrades", new ArrayList<>());

        gradeInfo.put("grade1", "-");
        gradeInfo.put("grade2", "-");
        gradeInfo.put("grade3", "-");

        for (Map<String, Object> period : periodGrades) {
            String periodName = (String) period.get("periodName");
            Double grade = ((Number) period.get("grade")).doubleValue();

            if ("Corte 1".equals(periodName)) {
                gradeInfo.put("grade1", formatGrade(grade));
            } else if ("Corte 2".equals(periodName)) {
                gradeInfo.put("grade2", formatGrade(grade));
            } else if ("Corte 3".equals(periodName)) {
                gradeInfo.put("grade3", formatGrade(grade));
            }
        }
    }

    private String formatGrade(Object grade) {
        if (grade == null) return "-";
        if (grade instanceof Number) {
            double value = ((Number) grade).doubleValue();
            return String.format("%.1f", value);
        }
        return grade.toString();
    }

    // Getters
    public List<Map<String, Object>> getCurrentSemesterGrades() {
        return currentSemesterGrades;
    }

    public String getCurrentSemester() {
        return currentSemester;
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
}