package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.controller.LoginController;
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

@Named
@ViewScoped
public class ProfileController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private LoginController loginController;

    private Map<String, Object> studentInfo;
    private Map<String, Object> academicRecord;
    private boolean loading = true;
    private boolean error = false;
    private String errorMessage;

    @PostConstruct
    public void init() {
        loadStudentInfo();
    }

    private void loadStudentInfo() {
        try {
            if (loginController == null || loginController.getId() == null) {
                error = true;
                errorMessage = "No se pudo obtener la información de sesión";
                loading = false;
                return;
            }

            // Get basic student info
            studentInfo = (Map<String, Object>) httpClientService.get(
                    "http://localhost:8080/api/students/" + loginController.getId(),
                    true
            );

            if (studentInfo == null) {
                error = true;
                errorMessage = "No se encontró información del estudiante";
                loading = false;
                return;
            }

            // Load academic record
            loadAcademicRecord();

        } catch (Exception e) {
            e.printStackTrace();
            error = true;
            errorMessage = "Error al cargar la información: " + e.getMessage();
        } finally {
            loading = false;
        }
    }

    private void loadAcademicRecord() {
        try {
            academicRecord = (Map<String, Object>) httpClientService.get(
                    "http://localhost:8080/api/students/" + loginController.getId() + "/academic-record",
                    true
            );

            if (academicRecord == null) {
                academicRecord = Map.of(
                        "completedSubjects", new ArrayList<>(),
                        "inProgressSubjects", new ArrayList<>(),
                        "pendingSubjects", new ArrayList<>()
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
            academicRecord = Map.of(
                    "completedSubjects", new ArrayList<>(),
                    "inProgressSubjects", new ArrayList<>(),
                    "pendingSubjects", new ArrayList<>()
            );
        }
    }



    public List<Map<String, Object>> getAllSubjects() {
        List<Map<String, Object>> all = new ArrayList<>();
        if (academicRecord != null) {
            // Map fields to maintain view consistency
            all.addAll(mapSubjects((List<Map<String, Object>>) academicRecord.getOrDefault("completedSubjects", new ArrayList<>())));
            all.addAll(mapSubjects((List<Map<String, Object>>) academicRecord.getOrDefault("inProgressSubjects", new ArrayList<>())));
            all.addAll(mapSubjects((List<Map<String, Object>>) academicRecord.getOrDefault("pendingSubjects", new ArrayList<>())));
        }
        return all;
    }

    private List<Map<String, Object>> mapSubjects(List<Map<String, Object>> subjects) {
        return subjects.stream().map(subject -> {
            Map<String, Object> mapped = new HashMap<>();
            mapped.put("subjectCode", subject.get("id"));
            mapped.put("subjectName", subject.get("name"));
            mapped.put("semester", subject.get("semester"));
            mapped.put("status", subject.get("status"));

            // Debug info
            System.out.println("Processing subject: " + subject);
            System.out.println("Subject ID: " + subject.get("id"));
            System.out.println("Subject Name: " + subject.get("name"));

            // Get final grade from specific endpoint
            try {
                String studentId = String.valueOf(loginController.getId());
                Object subjectIdObj = subject.get("id");

                if (studentId == null || subjectIdObj == null) {
                    System.out.println("Missing studentId or subjectId");
                    if (subject.containsKey("grade")) {
                        mapped.put("grade", subject.get("grade"));
                    }
                    return mapped;
                }

                String subjectId = subjectIdObj.toString();
                String endpoint = "http://localhost:8080/api/periods/calculate-subject-final/" +
                        studentId + "/" + subjectId;

                System.out.println("Calling endpoint: " + endpoint);

                Object response = httpClientService.get(endpoint, true);
                System.out.println("Endpoint response: " + response);
                System.out.println("Response type: " + (response != null ? response.getClass() : "null"));

                if (response instanceof Map) {
                    Map<String, Object> gradeInfo = (Map<String, Object>) response;
                    if (gradeInfo.containsKey("finalGrade")) {
                        mapped.put("grade", gradeInfo.get("finalGrade"));
                        System.out.println("Final grade obtained: " + gradeInfo.get("finalGrade"));
                    }
                } else if (response instanceof List) {
                    System.out.println("Received list response for grades - processing first grade if available");
                    List<?> gradeList = (List<?>) response;
                    if (!gradeList.isEmpty() && gradeList.get(0) instanceof Map) {
                        Map<?, ?> firstGrade = (Map<?, ?>) gradeList.get(0);
                        if (firstGrade.containsKey("finalGrade")) {  // ✅ CAMBIA A "finalGrade"
                            mapped.put("grade", firstGrade.get("finalGrade"));
                            System.out.println("Final grade from list: " + firstGrade.get("finalGrade"));
                        }
                    }
                }

                // Fallback to subject's grade if exists
                if (!mapped.containsKey("grade") && subject.containsKey("grade")) {
                    mapped.put("grade", subject.get("grade"));
                }
            } catch (RuntimeException e) {
                if (e.getMessage().contains("NO_GRADES_FOUND_FOR_SUBJECT")) {
                    System.out.println("No grades available for subject: " + subject.get("name"));
                } else {
                    e.printStackTrace();
                }
                if (subject.containsKey("grade")) {
                    mapped.put("grade", subject.get("grade"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (subject.containsKey("grade")) {
                    mapped.put("grade", subject.get("grade"));
                }
            }

            return mapped;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> getStudentInfo() {
        return studentInfo;
    }

    public Map<String, Object> getAcademicRecord() {
        return academicRecord;
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