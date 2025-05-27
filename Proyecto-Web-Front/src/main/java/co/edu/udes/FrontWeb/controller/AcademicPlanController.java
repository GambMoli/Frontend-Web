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
public class AcademicPlanController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private LoginController loginController;

    private Map<String, Object> studentInfo;
    private Map<String, Object> careerInfo;
    private List<Map<String, Object>> semestersWithSubjects;
    private boolean loading = true;
    private boolean error = false;
    private String errorMessage;

    @PostConstruct
    public void init() {
        loadAcademicPlan();
    }

    private void loadAcademicPlan() {
        try {
            System.out.println("Iniciando carga del plan académico...");

            // Verificar loginController
            if (loginController == null) {
                System.out.println("LoginController es null");
                error = true;
                errorMessage = "No se pudo obtener la información de sesión - LoginController null";
                loading = false;
                return;
            }

            if (loginController.getId() == null) {
                System.out.println("LoginController ID es null");
                error = true;
                errorMessage = "No se pudo obtener la información de sesión - ID null";
                loading = false;
                return;
            }

            System.out.println("Student ID from login: " + loginController.getId());

            // Get basic student info
            String endpoint = "http://localhost:8080/api/students/" + loginController.getId();
            System.out.println("Calling endpoint: " + endpoint);

            studentInfo = (Map<String, Object>) httpClientService.get(endpoint, true);
            System.out.println("Student info response: " + studentInfo);

            if (studentInfo == null) {
                System.out.println("Student info es null");
                error = true;
                errorMessage = "No se encontró información del estudiante";
                loading = false;
                return;
            }

            // Debug: mostrar contenido de studentInfo
            System.out.println("Student info keys: " + studentInfo.keySet());
            for (Map.Entry<String, Object> entry : studentInfo.entrySet()) {
                System.out.println("Key: " + entry.getKey() + ", Value: " + entry.getValue());
            }

            // Get career information
            loadCareerInfo();

        } catch (RuntimeException e) {
            System.out.println("RuntimeException en loadAcademicPlan: " + e.getMessage());
            e.printStackTrace();
            error = true;
            errorMessage = "Error de conexión: " + e.getMessage();
            loading = false;
        } catch (Exception e) {
            System.out.println("Exception en loadAcademicPlan: " + e.getMessage());
            e.printStackTrace();
            error = true;
            errorMessage = "Error al cargar la información: " + e.getMessage();
            loading = false;
        } finally {
            loading = false;
        }
    }

    private void loadCareerInfo() {
        try {
            System.out.println("Iniciando carga de información de carrera...");

            // Assuming student info contains career information
            Object careerData = studentInfo.get("career");
            System.out.println("Career data from student: " + careerData);

            if (careerData == null) {
                System.out.println("Career data es null - buscando alternativas...");

                // Try alternative field names
                careerData = studentInfo.get("carrera");
                if (careerData == null) {
                    careerData = studentInfo.get("Career");
                }
                if (careerData == null) {
                    careerData = studentInfo.get("careerId");
                }

                if (careerData == null) {
                    System.out.println("No se encontró información de carrera en ningún campo");
                    error = true;
                    errorMessage = "No se encontró información de la carrera del estudiante";
                    return;
                }
            }

            String careerId = null;

            if (careerData instanceof Map) {
                Map<String, Object> career = (Map<String, Object>) careerData;
                System.out.println("Career map keys: " + career.keySet());

                Object careerIdObj = career.get("id");
                if (careerIdObj == null) {
                    careerIdObj = career.get("careerId");
                }

                if (careerIdObj == null) {
                    System.out.println("No se pudo obtener el ID de la carrera del map");
                    error = true;
                    errorMessage = "No se pudo obtener el ID de la carrera";
                    return;
                }
                careerId = careerIdObj.toString();
            } else {
                // Si careerData no es un Map, asumimos que es directamente el ID
                careerId = careerData.toString();
            }

            System.out.println("Career ID obtenido: " + careerId);

            // Get complete career information with semesters and subjects
            String careerEndpoint = "http://localhost:8080/api/careers/" + careerId;
            System.out.println("Calling career endpoint: " + careerEndpoint);

            careerInfo = (Map<String, Object>) httpClientService.get(careerEndpoint, true);
            System.out.println("Career info response: " + careerInfo);

            if (careerInfo == null) {
                System.out.println("Career info es null");
                error = true;
                errorMessage = "No se encontró información de la carrera";
                return;
            }

            // Debug: mostrar contenido de careerInfo
            System.out.println("Career info keys: " + careerInfo.keySet());

            // Process semesters and subjects
            processSemestersAndSubjects();

        } catch (RuntimeException e) {
            System.out.println("RuntimeException en loadCareerInfo: " + e.getMessage());
            e.printStackTrace();
            error = true;
            errorMessage = "Error de conexión al cargar información de la carrera: " + e.getMessage();
        } catch (Exception e) {
            System.out.println("Exception en loadCareerInfo: " + e.getMessage());
            e.printStackTrace();
            error = true;
            errorMessage = "Error al cargar información de la carrera: " + e.getMessage();
        }
    }

    private void processSemestersAndSubjects() {
        try {
            System.out.println("Procesando semestres y materias...");
            semestersWithSubjects = new ArrayList<>();

            List<Map<String, Object>> semesters = (List<Map<String, Object>>) careerInfo.get("semesters");
            System.out.println("Semesters from career: " + semesters);

            if (semesters == null) {
                System.out.println("No se encontraron semestres - creando lista vacía");
                semesters = new ArrayList<>();
            }

            for (Map<String, Object> semester : semesters) {
                System.out.println("Procesando semestre: " + semester);

                Map<String, Object> semesterData = new HashMap<>();
                semesterData.put("number", semester.get("number"));
                semesterData.put("id", semester.get("id"));

                List<Map<String, Object>> subjects = (List<Map<String, Object>>) semester.get("subjects");
                List<Map<String, Object>> processedSubjects = new ArrayList<>();

                if (subjects != null) {
                    System.out.println("Materias en semestre " + semester.get("number") + ": " + subjects.size());
                    for (Map<String, Object> subject : subjects) {
                        Map<String, Object> processedSubject = processSubjectDetails(subject);
                        processedSubjects.add(processedSubject);
                    }
                } else {
                    System.out.println("No hay materias en semestre " + semester.get("number"));
                }

                semesterData.put("subjects", processedSubjects);
                semestersWithSubjects.add(semesterData);
            }

            // Sort semesters by number
            semestersWithSubjects.sort((s1, s2) -> {
                Integer num1 = (Integer) s1.get("number");
                Integer num2 = (Integer) s2.get("number");
                return num1.compareTo(num2);
            });

            System.out.println("Total semestres procesados: " + semestersWithSubjects.size());

        } catch (Exception e) {
            System.out.println("Error procesando semestres y materias: " + e.getMessage());
            e.printStackTrace();
            errorMessage = "Error al procesar semestres y materias: " + e.getMessage();
        }
    }

    private Map<String, Object> processSubjectDetails(Map<String, Object> basicSubject) {
        Map<String, Object> subjectDetails = new HashMap<>();

        try {
            Object subjectIdObj = basicSubject.get("id");
            if (subjectIdObj == null) {
                System.out.println("Subject ID es null, retornando subject básico");
                return basicSubject;
            }

            String subjectId = subjectIdObj.toString();
            System.out.println("Procesando detalles para materia ID: " + subjectId);

            // Get detailed subject information
            String subjectEndpoint = "http://localhost:8080/api/subjects/" + subjectId;
            Map<String, Object> detailedSubject = (Map<String, Object>) httpClientService.get(subjectEndpoint, true);

            if (detailedSubject != null) {
                subjectDetails.put("id", detailedSubject.get("id"));
                subjectDetails.put("name", detailedSubject.get("name"));

                // Get semester information
                Map<String, Object> semester = (Map<String, Object>) detailedSubject.get("semester");
                if (semester != null) {
                    subjectDetails.put("semesterNumber", semester.get("number"));
                }

                // Process prerequisites
                List<Map<String, Object>> prerequisites = (List<Map<String, Object>>) detailedSubject.get("prerequisites");
                if (prerequisites != null && !prerequisites.isEmpty()) {
                    String prerequisitesText = prerequisites.stream()
                            .map(prereq -> (String) prereq.get("name"))
                            .collect(Collectors.joining(", "));
                    subjectDetails.put("prerequisites", prerequisitesText);
                } else {
                    subjectDetails.put("prerequisites", "Ninguno");
                }

                // Get subject status (approved/not approved)
                getSubjectStatus(subjectDetails, subjectId);

            } else {
                System.out.println("No se pudo obtener detalles de la materia, usando info básica");
                // Fallback to basic info
                subjectDetails.put("id", basicSubject.get("id"));
                subjectDetails.put("name", basicSubject.get("name"));
                subjectDetails.put("prerequisites", "Ninguno");
                subjectDetails.put("status", "No cursada");
            }

        } catch (Exception e) {
            System.out.println("Error procesando detalles de materia: " + e.getMessage());
            e.printStackTrace();
            // Fallback to basic info in case of error
            subjectDetails.put("id", basicSubject.get("id"));
            subjectDetails.put("name", basicSubject.get("name"));
            subjectDetails.put("prerequisites", "Ninguno");
            subjectDetails.put("status", "No cursada");
        }

        return subjectDetails;
    }

    private void getSubjectStatus(Map<String, Object> subjectDetails, String subjectId) {
        try {
            String studentId = String.valueOf(loginController.getId());
            String endpoint = "http://localhost:8080/api/periods/calculate-subject-final/" +
                    studentId + "/" + subjectId;

            System.out.println("Obteniendo estado de materia: " + endpoint);

            Object response = httpClientService.get(endpoint, true);

            if (response instanceof Map) {
                Map<String, Object> gradeInfo = (Map<String, Object>) response;
                Boolean approved = (Boolean) gradeInfo.get("approved");

                if (approved != null) {
                    if (approved) {
                        subjectDetails.put("status", "Aprobada");
                        subjectDetails.put("statusClass", "badge bg-success");
                    } else {
                        subjectDetails.put("status", "Cursando");
                        subjectDetails.put("statusClass", "badge bg-warning text-dark");
                    }
                } else {
                    subjectDetails.put("status", "Pendiente");
                    subjectDetails.put("statusClass", "badge bg-info");
                }

                // Add grade if available
                Object grade = gradeInfo.get("finalGrade");
                if (grade != null) {
                    subjectDetails.put("grade", grade);
                }

            } else if (response instanceof List) {
                List<?> gradeList = (List<?>) response;
                if (!gradeList.isEmpty() && gradeList.get(0) instanceof Map) {
                    Map<?, ?> firstGrade = (Map<?, ?>) gradeList.get(0);
                    Boolean approved = (Boolean) firstGrade.get("approved");

                    if (approved != null) {
                        if (approved) {
                            subjectDetails.put("status", "Aprobada");
                            subjectDetails.put("statusClass", "badge bg-success");
                        } else {
                            subjectDetails.put("status", "Cursando");
                            subjectDetails.put("statusClass", "badge bg-warning text-dark");
                        }
                    } else {
                        subjectDetails.put("status", "Pendiente");
                        subjectDetails.put("statusClass", "badge bg-info");
                    }

                    Object grade = firstGrade.get("finalGrade");
                    if (grade != null) {
                        subjectDetails.put("grade", grade);
                    }
                } else {
                    subjectDetails.put("status", "No cursada");
                    subjectDetails.put("statusClass", "badge bg-secondary");
                }
            } else {
                subjectDetails.put("status", "No cursada");
                subjectDetails.put("statusClass", "badge bg-secondary");
            }

        } catch (RuntimeException e) {
            if (e.getMessage().contains("NO_GRADES_FOUND_FOR_SUBJECT")) {
                subjectDetails.put("status", "No cursada");
                subjectDetails.put("statusClass", "badge bg-secondary");
            } else {
                System.out.println("RuntimeException obteniendo estado de materia: " + e.getMessage());
                subjectDetails.put("status", "No cursada");
                subjectDetails.put("statusClass", "badge bg-secondary");
            }
        } catch (Exception e) {
            System.out.println("Exception obteniendo estado de materia: " + e.getMessage());
            subjectDetails.put("status", "No cursada");
            subjectDetails.put("statusClass", "badge bg-secondary");
        }
    }

    // Getters
    public Map<String, Object> getStudentInfo() {
        return studentInfo;
    }

    public Map<String, Object> getCareerInfo() {
        return careerInfo;
    }

    public List<Map<String, Object>> getSemestersWithSubjects() {
        return semestersWithSubjects;
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

    // Helper methods for the view
    public String getCareerName() {
        if (careerInfo != null) {
            return (String) careerInfo.get("name");
        }
        return "N/A";
    }

    public String getStudentName() {
        if (studentInfo != null) {
            String firstName = (String) studentInfo.get("firstName");
            String lastName = (String) studentInfo.get("lastName");
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
        }
        return "N/A";
    }

    public int getTotalSemesters() {
        if (semestersWithSubjects != null) {
            return semestersWithSubjects.size();
        }
        return 0;
    }

    public int getCurrentSemester() {
        // Logic to determine current semester based on approved subjects
        if (semestersWithSubjects != null) {
            for (Map<String, Object> semester : semestersWithSubjects) {
                List<Map<String, Object>> subjects = (List<Map<String, Object>>) semester.get("subjects");
                if (subjects != null) {
                    boolean allApproved = true;

                    for (Map<String, Object> subject : subjects) {
                        String status = (String) subject.get("status");
                        if (!"Aprobada".equals(status)) {
                            allApproved = false;
                            break;
                        }
                    }

                    if (!allApproved) {
                        return (Integer) semester.get("number");
                    }
                }
            }
        }
        return 1;
    }
}