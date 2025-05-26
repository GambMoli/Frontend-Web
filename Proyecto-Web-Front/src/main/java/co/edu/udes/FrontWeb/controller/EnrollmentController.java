package co.edu.udes.FrontWeb.controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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
    private String careerName = "";
    private int currentSemester = 1;

    // Enrollment data
    private List<Map<String, Object>> enrolledClasses = new ArrayList<>();
    private List<Map<String, Object>> allSubjects = new ArrayList<>();
    private List<Map<String, Object>> filteredSubjects = new ArrayList<>();
    private String searchTerm = "";

    // Career subjects for filtering
    private List<Integer> careerSubjectIds = new ArrayList<>();

    public void init() {
        try {
            // Verificar autenticación
            if (loginController == null || loginController.getId() <= 0) {
                return;
            }

            loadStudentData();
            loadCareerSubjects();
            loadEnrolledClasses();
            loadAvailableSubjects();

        } catch (Exception e) {
            System.err.println("Error en init(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadStudentData() {
        try {
            String token = getAuthToken();
            if (token == null) {
                return;
            }

            int studentId = loginController.getId();
            String academicRecordUrl = "http://localhost:8080/api/students/" + studentId + "/academic-record";

            Object response = httpClientService.get(academicRecordUrl, true);

            if (response instanceof Map) {
                Map<String, Object> academicRecord = (Map<String, Object>) response;

                Object studentNameObj = academicRecord.get("studentName");
                if (studentNameObj != null) {
                    this.studentName = studentNameObj.toString();
                } else {
                    this.studentName = "Estudiante";
                }

                Object careerNameObj = academicRecord.get("careerName");
                if (careerNameObj != null) {
                    this.careerName = careerNameObj.toString();
                } else {
                    this.careerName = "Carrera no definida";
                }

                Object currentSemesterObj = academicRecord.get("currentSemester");
                if (currentSemesterObj != null) {
                    try {
                        if (currentSemesterObj instanceof Integer) {
                            this.currentSemester = (Integer) currentSemesterObj;
                        } else {
                            this.currentSemester = Integer.parseInt(currentSemesterObj.toString());
                        }
                    } catch (NumberFormatException e) {
                        this.currentSemester = 1;
                    }
                } else {
                    this.currentSemester = 1;
                }
            } else {
                this.studentName = "Estudiante";
                this.careerName = "Carrera no definida";
                this.currentSemester = 1;
            }

        } catch (Exception e) {
            System.err.println("Error en loadStudentData(): " + e.getMessage());
            e.printStackTrace();
            this.studentName = "Estudiante";
            this.careerName = "Carrera no definida";
            this.currentSemester = 1;
        }
    }

    private void loadCareerSubjects() {
        try {
            String token = getAuthToken();
            if (token == null) {
                careerSubjectIds.clear();
                return;
            }

            String careersUrl = "http://localhost:8080/api/careers";
            Object response = httpClientService.get(careersUrl, true);
            careerSubjectIds.clear();

            if (response instanceof List) {
                List<Map<String, Object>> careers = (List<Map<String, Object>>) response;

                for (Map<String, Object> career : careers) {
                    Object careerNameObj = career.get("name");
                    if (careerNameObj != null && careerNameObj.toString().equals(this.careerName)) {
                        Object semestersObj = career.get("semesters");
                        if (semestersObj instanceof List) {
                            List<Map<String, Object>> semesters = (List<Map<String, Object>>) semestersObj;

                            for (Map<String, Object> semester : semesters) {
                                Object subjectsObj = semester.get("subjects");
                                if (subjectsObj instanceof List) {
                                    List<Map<String, Object>> subjects = (List<Map<String, Object>>) subjectsObj;

                                    for (Map<String, Object> subject : subjects) {
                                        Object subjectIdObj = subject.get("id");
                                        if (subjectIdObj != null) {
                                            try {
                                                int subjectId;
                                                if (subjectIdObj instanceof Integer) {
                                                    subjectId = (Integer) subjectIdObj;
                                                } else {
                                                    subjectId = Integer.parseInt(subjectIdObj.toString());
                                                }
                                                careerSubjectIds.add(subjectId);
                                            } catch (NumberFormatException e) {
                                                // Ignorar IDs inválidos
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break; // Encontramos la carrera, salir del loop
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error en loadCareerSubjects(): " + e.getMessage());
            e.printStackTrace();
            careerSubjectIds.clear();
        }
    }

    private String getAuthToken() {
        try {
            // En este contexto no tenemos acceso a FacesContext, pero el token debería ser manejado por el HttpClientService
            return "token"; // Placeholder - el HttpClientService debería manejar esto
        } catch (Exception e) {
            System.err.println("Error al obtener token: " + e.getMessage());
            return null;
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
                                        uniqueClasses.put(groupId, enrolledClass);
                                    }
                                }
                            }
                        }
                    }

                    enrolledClasses.addAll(uniqueClasses.values());
                }
            }

        } catch (Exception e) {
            System.err.println("Error en loadEnrolledClasses(): " + e.getMessage());
            e.printStackTrace();
            enrolledClasses.clear();
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

                            // Filtrar solo materias de la carrera del estudiante
                            if (!careerSubjectIds.contains(subjectId)) {
                                continue;
                            }

                            String subjectName = subjectNameObj.toString();

                            if (!subjectsMap.containsKey(subjectId)) {
                                Map<String, Object> subject = new HashMap<>();
                                subject.put("id", subjectId);
                                subject.put("name", subjectName);
                                subject.put("code", "SUB" + subjectId);
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

            // Verificar si algún grupo de esta materia está matriculado
            for (Map<String, Object> enrolledClass : enrolledClasses) {
                Object enrolledGroupIdObj = enrolledClass.get("groupId");
                if (enrolledGroupIdObj == null) continue;

                try {
                    int enrolledGroupId;
                    if (enrolledGroupIdObj instanceof Integer) {
                        enrolledGroupId = (Integer) enrolledGroupIdObj;
                    } else {
                        enrolledGroupId = Integer.parseInt(enrolledGroupIdObj.toString());
                    }

                    // Buscar si este grupo pertenece a la materia
                    for (Map<String, Object> subject : allSubjects) {
                        Object subjectIdInList = subject.get("id");
                        if (subjectIdInList != null) {
                            int subjectIdInListInt;
                            if (subjectIdInList instanceof Integer) {
                                subjectIdInListInt = (Integer) subjectIdInList;
                            } else {
                                subjectIdInListInt = Integer.parseInt(subjectIdInList.toString());
                            }

                            if (subjectIdInListInt == subjectId) {
                                Object groupsObj = subject.get("groups");
                                if (groupsObj instanceof List) {
                                    List<Map<String, Object>> groups = (List<Map<String, Object>>) groupsObj;
                                    for (Map<String, Object> group : groups) {
                                        Object groupIdObj = group.get("id");
                                        if (groupIdObj != null) {
                                            int groupId;
                                            if (groupIdObj instanceof Integer) {
                                                groupId = (Integer) groupIdObj;
                                            } else {
                                                groupId = Integer.parseInt(groupIdObj.toString());
                                            }
                                            if (groupId == enrolledGroupId) {
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    continue;
                }
            }

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

        } catch (Exception e) {
            System.err.println("Error en enrollGroup(): " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public String cancelEnrollment(Object groupIdObj) {
        try {
            if (groupIdObj == null) {
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

            // Recargar datos del servidor para asegurar consistencia
            loadEnrolledClasses();
            loadAvailableSubjects();

        } catch (Exception e) {
            System.err.println("Error en cancelEnrollment(): " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void refreshData() {
        try {
            loadStudentData();
            loadCareerSubjects();
            loadEnrolledClasses();
            loadAvailableSubjects();
        } catch (Exception e) {
            System.err.println("Error en refreshData(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters y setters adicionales para JSF
    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm != null ? searchTerm : "";
        filterSubjects();
    }

    // Getters explícitos necesarios para JSF
    public String getStudentName() {
        return studentName;
    }

    public String getCareerName() {
        return careerName;
    }

    public int getCurrentSemester() {
        return currentSemester;
    }

    public List<Map<String, Object>> getEnrolledClasses() {
        return enrolledClasses;
    }

    public List<Map<String, Object>> getAllSubjects() {
        return allSubjects;
    }

    public List<Map<String, Object>> getFilteredSubjects() {
        return filteredSubjects;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public List<Integer> getCareerSubjectIds() {
        return careerSubjectIds;
    }
}