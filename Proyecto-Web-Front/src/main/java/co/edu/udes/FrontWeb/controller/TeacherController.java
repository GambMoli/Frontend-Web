package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.*;
import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Named("teacherController")
@SessionScoped
public class TeacherController implements Serializable {
    @Inject
    private HttpClientService httpClientService;
    @Inject
    private LoginController loginController;


    private List<Group> listaGrupos = new ArrayList<>();
    private List<Reserve> listReserve = new ArrayList<>();
    public List<Loan> listLoans= new ArrayList<>();
    private Group selectedGroup;
    private Long idGrupoSeleccionado;

    private String periodoSeleccionado;
    private Double nota;
    private Long subjectIdSeleccionado;
    private Long periodIdSeleccionado;
    private Long estudianteSeleccionado;

    private static final String API_URL = "http://localhost:8080/api/teachers";
    //Getters y setters


    public String getPeriodoSeleccionado() {
        return periodoSeleccionado;
    }

    public Long getPeriodIdSeleccionado() {
        return periodIdSeleccionado;
    }

    public void setPeriodIdSeleccionado(Long periodIdSeleccionado) {
        this.periodIdSeleccionado = periodIdSeleccionado;
    }

    public void setPeriodoSeleccionado(String periodoSeleccionado) {
        this.periodoSeleccionado = periodoSeleccionado;
    }

    public Long getEstudianteSeleccionado() {
        return estudianteSeleccionado;
    }

    public void setEstudianteSeleccionado(Long estudianteSeleccionado) {
        this.estudianteSeleccionado = estudianteSeleccionado;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public Long getSubjectIdSeleccionado() {
        return subjectIdSeleccionado;
    }

    public void setSubjectIdSeleccionado(Long subjectIdSeleccionado) {
        this.subjectIdSeleccionado = subjectIdSeleccionado;
    }

    public List<Loan> getListLoans() {
        return listLoans;
    }

    public void setListLoans(List<Loan> listLoans) {
        this.listLoans = listLoans;
    }

    public Long getIdGrupoSeleccionado() {
        return idGrupoSeleccionado;
    }

    public void setIdGrupoSeleccionado(Long idGrupoSeleccionado) {
        this.idGrupoSeleccionado = idGrupoSeleccionado;
    }

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public List<Group> getListaGrupos() {
        return listaGrupos;
    }

    public void setListaGrupos(List<Group> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    public List<Reserve> getListReserve() {
        return listReserve;
    }

    public void setListReserve(List<Reserve> listReserve) {
        this.listReserve = listReserve;
    }

    @PostConstruct
    public void init() {
        System.out.println("Se ejecuta init() de GroupController");
        System.out.println("Desde session: " + loginController.getId());
        System.out.println("Desde loginController.getId(): " + loginController.getId());

        Long id = loginController.getId().longValue();// o usa getUserIdFromSession()
        if (id != null) {
            ListarGrupos(id);
            ListarReservas(id);
            ListarPrestamos(id);
        } else {
            System.out.println("ID de usuario es null, no se puede listar grupos");
        }
    }
    public void ListarGrupos(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            listaGrupos = (List<Group>) httpClientService.get(API_URL + "/" + id + "/myGroups", true);
            context.getExternalContext().getSessionMap().put("ListaGrupos", listaGrupos);

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }
    public void ListarReservas(Long id){
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            listReserve = (List<Reserve>) httpClientService.get(API_URL + "/" + id + "/my_reserve", true);
            context.getExternalContext().getSessionMap().put("ListaReservas", listReserve);

        }catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }

    }

    public void ListarPrestamos(Long id){
        FacesContext context = FacesContext.getCurrentInstance();
         try {
            listLoans = (List<Loan>) httpClientService.get(API_URL + "/" + id + "/my_loans", true);
            context.getExternalContext().getSessionMap().put("ListaLoans", listLoans);

         }catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
         }
    }
    public String obtenerNombreMaterial(Loan loan) {
        if (loan == null || loan.getMaterial() == null) {
            return "Material no disponible";
        }
        return loan.getMaterial().getName(); // Ajusta según tu estructura de clases
    }
    public void guardarNota() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            System.out.println("===> Iniciando guardarNota()");
            System.out.println("SUBJECTID: " + subjectIdSeleccionado);
            System.out.println("Periodo Seleccionado: " + periodoSeleccionado);
            System.out.println("ID Grupo Seleccionado (subjectId): " + idGrupoSeleccionado);
            System.out.println("Nota: " + nota);

            Long periodId = null;

            switch (periodoSeleccionado) {
                case "PRIMERO":
                    periodId = 1L;
                    break;
                case "SEGUNDO":
                    periodId = 2L;
                    break;
                case "TERCERO":
                    periodId = 3L;
                    break;
                default:
                    throw new IllegalArgumentException("Periodo no válido: " + periodoSeleccionado);
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("periodId", periodId);
            payload.put("subjectId", subjectIdSeleccionado);
            payload.put("value", nota);

            System.out.println("Payload a enviar: " + payload);

            Object response = httpClientService.post("http://localhost:8080/api/periods/grades", payload, true);
            System.out.println("Respuesta del servidor: " + response);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Nota guardada correctamente", null));

            // Limpia los valores después de guardar
            nota = null;
            periodoSeleccionado = null;

        } catch (Exception e) {
            System.out.println("===> Error al guardar la nota");
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al guardar la nota", null));
        }
    }

    public void seleccionarEstudiante(Long studentId) {
        System.out.println("Estudiante seleccionado: " + studentId);
        this.estudianteSeleccionado = studentId != null ? studentId.longValue() : null;
    }
    public String verEstudiantesPorGrupo() {
        System.out.println("Grupo seleccionado: " + idGrupoSeleccionado);
        System.out.println("Materia seleccionada: " + subjectIdSeleccionado);
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("idGrupo", idGrupoSeleccionado);
        return "studentsByGroup.xhtml?faces-redirect=true";
    }

    public void inicializarPeriodosParaEstudiante() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            List<PeriodConfiguration> configuraciones = new ArrayList<>();
            configuraciones.add(new PeriodConfiguration("Corte 1", 0.3, LocalDate.of(2025, 2, 3), LocalDate.of(2025, 3, 15)));
            configuraciones.add(new PeriodConfiguration("Corte 2", 0.3, LocalDate.of(2025, 3, 16), LocalDate.of(2025, 4, 30)));
            configuraciones.add(new PeriodConfiguration("Corte 3", 0.4, LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 15)));

            PeriodConfigRequest request = new PeriodConfigRequest(estudianteSeleccionado, configuraciones);

            System.out.println("Enviando inicialización de periodos para estudiante: " + estudianteSeleccionado);
            Object response = httpClientService.post("http://localhost:8080/api/periods/init", request, true);

            System.out.println("Respuesta: " + response);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Periodos inicializados correctamente", null));

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al inicializar periodos", null));
        }
    }
}
