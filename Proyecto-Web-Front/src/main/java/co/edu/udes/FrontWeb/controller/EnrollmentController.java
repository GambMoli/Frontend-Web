package co.edu.udes.FrontWeb.controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import co.edu.udes.FrontWeb.service.HttpClientService;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Named("enrollmentController")
@SessionScoped
@Data
public class EnrollmentController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private LoginController loginController;

    // Student information
    private String studentName = "";
    private String careerName = "Ingeniería de Sistemas";
    private int currentSemester = 2024;
    private int enrolledCredits = 0;

    // Enrollment data
    private List<Map<String, Object>> enrolledClasses = new ArrayList<>();
    private List<Map<String, Object>> allSubjects = new ArrayList<>();
    private List<Map<String, Object>> filteredSubjects = new ArrayList<>();
    private String searchTerm = "";

    public void init() {
        try {
            // Limpiar mensajes anteriores
            clearMessages();

            // Verificar autenticación
            if (loginController == null || loginController.getId() <= 0) {
                showErrorMessage("Sesión no válida. Por favor, inicie sesión nuevamente.");
                return;
            }

            loadStudentData();
            loadEnrolledClasses();
            loadAvailableSubjects();

        } catch (Exception e) {
            System.err.println("Error en init(): " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Error al inicializar la página. Por favor, recargue la página.");
        }
    }

    private void loadStudentData() {
        try {
            String token = getAuthToken();
            if (token == null) {
                showErrorMessage("Token de autenticación no encontrado");
                return;
            }

            int studentId = loginController.getId();
            String scheduleUrl = "http://localhost:8080/api/students/" + studentId + "/schedule";

            Object response = httpClientService.get(scheduleUrl, true);

            if (response instanceof Map) {
                Map<String, Object> scheduleResponse = (Map<String, Object>) response;

                Object studentNameObj = scheduleResponse.get("studentName");
                if (studentNameObj != null) {
                    this.studentName = studentNameObj.toString();
                } else {
                    this.studentName = "Estudiante";
                }

                this.careerName = "Ingeniería de Sistemas";
                this.enrolledCredits = calculateEnrolledCredits();
            } else {
                this.studentName = "Estudiante";
                showWarningMessage("No se pudieron cargar todos los datos del estudiante");
            }

        } catch (Exception e) {
            System.err.println("Error en loadStudentData(): " + e.getMessage());
            e.printStackTrace();
            this.studentName = "Estudiante";
            showErrorMessage("Error al cargar datos del estudiante");
        }
    }

    private String getAuthToken() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null && facesContext.getExternalContext() != null) {
                return (String) facesContext.getExternalContext()
                        .getSessionMap().get("authToken");
            }
            return null;
        } catch (Exception e) {
            System.err.println("Error al obtener token: " + e.getMessage());
            return null;
        }
    }

    private int calculateEnrolledCredits() {
        try {
            return enrolledClasses.stream()
                    .mapToInt(clazz -> {
                        Object credits = clazz.get("credits");
                        if (credits == null) return 0;

                        try {
                            if (credits instanceof Integer) {
                                return (Integer) credits;
                            } else {
                                return Integer.parseInt(credits.toString());
                            }
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (Exception e) {
            System.err.println("Error calculando créditos: " + e.getMessage());
            return 0;
        }
    }

    public void loadEnrolledClasses() {
        try {
            String token = getAuthToken();
            if (token == null) {
                enrolledClasses.clear();
                return;
            }

            int studentId = loginController.getId();
            String scheduleUrl = "http://localhost:8080/api/students/" + studentId + "/schedule";

            Object response = httpClientService.get(scheduleUrl, true);
            enrolledClasses.clear();

            if (response instanceof Map) {
                Map<String, Object> scheduleResponse = (Map<String, Object>) response;
                Object weekScheduleObj = scheduleResponse.get("weekSchedule");

                if (weekScheduleObj instanceof List) {
                    List<Map<String, Object>> weekSchedule = (List<Map<String, Object>>) weekScheduleObj;
                    Map<Integer, Map<String, Object>> uniqueClasses = new HashMap<>();

                    for (Map<String, Object> daySchedule : weekSchedule) {
                        Object classesObj = daySchedule.get("classes");
                        if (classesObj instanceof List) {
                            List<Map<String, Object>> classes = (List<Map<String, Object>>) classesObj;

                            for (Map<String, Object> classInfo : classes) {
                                Object groupIdObj = classInfo.get("groupId");
                                if (groupIdObj != null) {
                                    Integer groupId = null;
                                    if (groupIdObj instanceof Integer) {
                                        groupId = (Integer) groupIdObj;
                                    } else {
                                        try {
                                            groupId = Integer.parseInt(groupIdObj.toString());
                                        } catch (NumberFormatException e) {
                                            continue;
                                        }
                                    }

                                    if (groupId != null && !uniqueClasses.containsKey(groupId)) {
                                        Map<String, Object> enrolledClass = new HashMap<>();
                                        enrolledClass.put("groupId", groupId);

                                        Object subjectName = classInfo.get("subjectName");
                                        enrolledClass.put("subjectName", subjectName != null ? subjectName.toString() : "Materia");

                                        Object subjectId = classInfo.get("subjectId");
                                        enrolledClass.put("subjectCode", "SUB" + (subjectId != null ? subjectId.toString() : "0"));

                                        enrolledClass.put("groupCode", "G" + groupId);
                                        enrolledClass.put("credits", 3);
                                        uniqueClasses.put(groupId, enrolledClass);
                                    }
                                }
                            }
                        }
                    }

                    enrolledClasses.addAll(uniqueClasses.values());
                }
            }

            enrolledCredits = calculateEnrolledCredits();

        } catch (Exception e) {
            System.err.println("Error en loadEnrolledClasses(): " + e.getMessage());
            e.printStackTrace();
            enrolledClasses.clear();
            enrolledCredits = 0;
            showErrorMessage("Error al cargar materias matriculadas");
        }
    }

    public void loadAvailableSubjects() {
        try {
            String token = getAuthToken();
            if (token == null) {
                allSubjects.clear();
                filteredSubjects.clear();
                return;
            }

            String groupsUrl = "http://localhost:8080/api/groups";
            Object response = httpClientService.get(groupsUrl, true);
            allSubjects.clear();

            if (response instanceof List) {
                List<Map<String, Object>> groupsResponse = (List<Map<String, Object>>) response;
                Map<Integer, Map<String, Object>> subjectsMap = new HashMap<>();

                for (Map<String, Object> group : groupsResponse) {
                    try {
                        Object subjectIdObj = group.get("subjectId");
                        Object subjectNameObj = group.get("subjectName");

                        if (subjectIdObj != null && subjectNameObj != null) {
                            Integer subjectId = null;
                            if (subjectIdObj instanceof Integer) {
                                subjectId = (Integer) subjectIdObj;
                            } else {
                                try {
                                    subjectId = Integer.parseInt(subjectIdObj.toString());
                                } catch (NumberFormatException e) {
                                    continue;
                                }
                            }

                            String subjectName = subjectNameObj.toString();

                            if (!subjectsMap.containsKey(subjectId)) {
                                Map<String, Object> subject = new HashMap<>();
                                subject.put("id", subjectId);
                                subject.put("name", subjectName);
                                subject.put("code", "SUB" + subjectId);
                                subject.put("credits", 3);
                                subject.put("groups", new ArrayList<Map<String, Object>>());
                                subjectsMap.put(subjectId, subject);
                            }

                            Map<String, Object> groupData = new HashMap<>();
                            groupData.put("id", group.get("id"));

                            Object groupName = group.get("name");
                            groupData.put("code", groupName != null ? groupName.toString() : "G1");

                            Object capacityObj = group.get("capacity");
                            Object enrolledObj = group.get("enrolledCount");

                            int capacity = 0;
                            int enrolled = 0;

                            if (capacityObj instanceof Integer) {
                                capacity = (Integer) capacityObj;
                            } else if (capacityObj != null) {
                                try {
                                    capacity = Integer.parseInt(capacityObj.toString());
                                } catch (NumberFormatException e) {
                                    capacity = 30; // valor por defecto
                                }
                            }

                            if (enrolledObj instanceof Integer) {
                                enrolled = (Integer) enrolledObj;
                            } else if (enrolledObj != null) {
                                try {
                                    enrolled = Integer.parseInt(enrolledObj.toString());
                                } catch (NumberFormatException e) {
                                    enrolled = 0;
                                }
                            }

                            groupData.put("availableSlots", Math.max(0, capacity - enrolled));
                            groupData.put("enrolledStudents", enrolled);
                            groupData.put("maxStudents", capacity);

                            String classroom = "Aula por definir";
                            Object schedulesObj = group.get("schedules");
                            if (schedulesObj instanceof List) {
                                List<Map<String, Object>> schedules = (List<Map<String, Object>>) schedulesObj;
                                if (!schedules.isEmpty()) {
                                    Object classroomObj = schedules.get(0).get("classroom");
                                    if (classroomObj != null) {
                                        classroom = classroomObj.toString();
                                    }
                                }
                                groupData.put("schedules", schedules);
                            } else {
                                groupData.put("schedules", new ArrayList<>());
                            }

                            groupData.put("classroom", classroom);
                            groupData.put("teacherName", "Profesor por asignar");

                            ((List<Map<String, Object>>) subjectsMap.get(subjectId).get("groups")).add(groupData);
                        }
                    } catch (Exception e) {
                        System.err.println("Error procesando grupo: " + e.getMessage());
                        // Continúa con el siguiente grupo
                    }
                }

                allSubjects.addAll(subjectsMap.values());
            }

            filterSubjects();

        } catch (Exception e) {
            System.err.println("Error en loadAvailableSubjects(): " + e.getMessage());
            e.printStackTrace();
            allSubjects.clear();
            filteredSubjects.clear();
            showErrorMessage("Error al cargar materias disponibles");
        }
    }

    public boolean isSubjectEnrolled(Object subjectIdObj) {
        if (subjectIdObj == null) return false;

        try {
            int subjectId;
            if (subjectIdObj instanceof Integer) {
                subjectId = (Integer) subjectIdObj;
            } else {
                subjectId = Integer.parseInt(subjectIdObj.toString());
            }

            // Por simplicidad, devolvemos false - puedes implementar la lógica real aquí
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void filterSubjects() {
        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                filteredSubjects = new ArrayList<>(allSubjects);
            } else {
                String lowerCaseFilter = searchTerm.toLowerCase().trim();
                filteredSubjects = allSubjects.stream()
                        .filter(subject -> {
                            Object nameObj = subject.get("name");
                            Object codeObj = subject.get("code");

                            String name = nameObj != null ? nameObj.toString().toLowerCase() : "";
                            String code = codeObj != null ? codeObj.toString().toLowerCase() : "";

                            return name.contains(lowerCaseFilter) || code.contains(lowerCaseFilter);
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.err.println("Error en filterSubjects(): " + e.getMessage());
            filteredSubjects = new ArrayList<>(allSubjects);
        }
    }

    public boolean isGroupEnrolled(Object groupIdObj) {
        if (groupIdObj == null) return false;

        try {
            int groupId;
            if (groupIdObj instanceof Integer) {
                groupId = (Integer) groupIdObj;
            } else {
                groupId = Integer.parseInt(groupIdObj.toString());
            }

            return enrolledClasses.stream()
                    .anyMatch(clazz -> {
                        Object enrolledGroupIdObj = clazz.get("groupId");
                        if (enrolledGroupIdObj == null) return false;

                        try {
                            int enrolledGroupId;
                            if (enrolledGroupIdObj instanceof Integer) {
                                enrolledGroupId = (Integer) enrolledGroupIdObj;
                            } else {
                                enrolledGroupId = Integer.parseInt(enrolledGroupIdObj.toString());
                            }
                            return enrolledGroupId == groupId;
                        } catch (Exception e) {
                            return false;
                        }
                    });
        } catch (Exception e) {
            return false;
        }
    }

    public String enrollGroup(Object groupIdObj) {
        try {
            if (groupIdObj == null) {
                showErrorMessage("ID de grupo inválido");
                return null;
            }

            int groupId;
            if (groupIdObj instanceof Integer) {
                groupId = (Integer) groupIdObj;
            } else {
                groupId = Integer.parseInt(groupIdObj.toString());
            }

            String token = getAuthToken();
            if (token == null) {
                showErrorMessage("Sesión expirada. Por favor, inicie sesión nuevamente.");
                return null;
            }

            int studentId = loginController.getId();
            String enrollUrl = "http://localhost:8080/api/students/enroll-group";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("studentId", studentId);
            requestBody.put("groupId", groupId);

            Object response = httpClientService.post(enrollUrl, requestBody, true);

            // Refresh data after successful enrollment
            loadEnrolledClasses();
            loadAvailableSubjects();

            showSuccessMessage("Matrícula realizada con éxito");

        } catch (Exception e) {
            System.err.println("Error en enrollGroup(): " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Error al matricular grupo: " + e.getMessage());
        }

        return null;
    }

    public String cancelEnrollment(Object groupIdObj) {
        try {
            if (groupIdObj == null) {
                showErrorMessage("ID de grupo inválido");
                return null;
            }

            int groupId;
            if (groupIdObj instanceof Integer) {
                groupId = (Integer) groupIdObj;
            } else {
                groupId = Integer.parseInt(groupIdObj.toString());
            }

            String token = getAuthToken();
            if (token == null) {
                showErrorMessage("Sesión expirada. Por favor, inicie sesión nuevamente.");
                return null;
            }

            int studentId = loginController.getId();
            String cancelUrl = "http://localhost:8080/api/students/cancel-enrollment";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("studentId", studentId);
            requestBody.put("groupId", groupId);

            Object response = httpClientService.post(cancelUrl, requestBody, true);

            // Eliminar inmediatamente de la lista local
            enrolledClasses.removeIf(clazz -> {
                Object enrolledGroupIdObj = clazz.get("groupId");
                if (enrolledGroupIdObj == null) return false;

                try {
                    int enrolledGroupId;
                    if (enrolledGroupIdObj instanceof Integer) {
                        enrolledGroupId = (Integer) enrolledGroupIdObj;
                    } else {
                        enrolledGroupId = Integer.parseInt(enrolledGroupIdObj.toString());
                    }
                    return enrolledGroupId == groupId;
                } catch (Exception e) {
                    return false;
                }
            });

            // Recalcular créditos
            enrolledCredits = calculateEnrolledCredits();

            // Recargar datos del servidor para asegurar consistencia
            loadEnrolledClasses();
            loadAvailableSubjects();

            showSuccessMessage("Matrícula cancelada con éxito");

        } catch (Exception e) {
            System.err.println("Error en cancelEnrollment(): " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Error al cancelar matrícula: " + e.getMessage());
        }

        return null;
    }

    public void refreshData() {
        try {
            loadStudentData();
            loadEnrolledClasses();
            loadAvailableSubjects();
            showSuccessMessage("Datos actualizados correctamente");
        } catch (Exception e) {
            System.err.println("Error en refreshData(): " + e.getMessage());
            e.printStackTrace();
            showErrorMessage("Error al actualizar datos");
        }
    }

    public void clearMessages() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.getMessageList().clear();
            }
        } catch (Exception e) {
            System.err.println("Error clearing messages: " + e.getMessage());
        }
    }

    private void showSuccessMessage(String message) {
        addMessage(FacesMessage.SEVERITY_INFO, "Éxito", message);
    }

    private void showErrorMessage(String message) {
        addMessage(FacesMessage.SEVERITY_ERROR, "Error", message);
    }

    private void showWarningMessage(String message) {
        addMessage(FacesMessage.SEVERITY_WARN, "Advertencia", message);
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        try {
            if (detail != null && !detail.trim().isEmpty()) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                if (facesContext != null) {
                    facesContext.addMessage(null, new FacesMessage(severity, summary, detail));
                }
            }
        } catch (Exception e) {
            System.err.println("Error adding message: " + e.getMessage());
        }
    }

    // Getters y setters adicionales para JSF
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm != null ? searchTerm : "";
        filterSubjects();
    }
}