package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.Place;
import co.edu.udes.FrontWeb.model.Reserve;
import co.edu.udes.FrontWeb.model.Student;
import co.edu.udes.FrontWeb.model.Teacher;
import co.edu.udes.FrontWeb.service.HttpClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import com.fasterxml.jackson.core.type.TypeReference;

import javax.swing.plaf.synth.SynthTextAreaUI;
import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named("reserveController")
@SessionScoped
public class ReserveController implements Serializable {

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private LoginController loginController;

    @Inject
    private TeacherController teacherController;

    @Inject
    private StudentController studentController;

    private String reserveDate;
    private String hourInit;
    private String hourFinish;
    private Long placeId;
    private Reserve reserve;
    private Long idReservaSeleccionada;

    // Datos del usuario en sesión
    private Long studentId;
    private Long teacherId;
    private String userType; // "student" o "teacher"

    private List<Place> listaLugares = new ArrayList<>();
    private List<Reserve> listaReservas= new ArrayList<>();
    private static final String API_URL = "http://localhost:8080/api/reserve";

    // GETTERS & SETTERS
    public Long getIdReservaSeleccionada() {
        return idReservaSeleccionada;
    }

    public void setIdReservaSeleccionada(Long idReservaSeleccionada) {
        this.idReservaSeleccionada = idReservaSeleccionada;
    }
    public String getReserveDate() {
        return reserveDate;
    }

    public void setReserveDate(String reserveDate) {
        this.reserveDate = reserveDate;
    }

    public String getHourInit() {
        return hourInit;
    }

    public void setHourInit(String hourInit) {
        this.hourInit = hourInit;
    }

    public String getHourFinish() {
        return hourFinish;
    }

    public void setHourFinish(String hourFinish) {
        this.hourFinish = hourFinish;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public List<Reserve> getListaReservas() {
        return listaReservas;
    }

    public void setListaReservas(List<Reserve> listaReservas) {
        this.listaReservas = listaReservas;
    }

    public void setListaLugares(List<Place> listaLugares) {
        this.listaLugares = listaLugares;
    }


    public Reserve getReserve() {
        return reserve;
    }

    public void setReserve(Reserve reserve) {
        this.reserve = reserve;
    }



    // Cargar lugares al iniciar
    @PostConstruct
    public void init() {
        System.out.println("Se ejecuta init() de ReserveController");

        if (loginController != null && loginController.getId() != null && loginController.getRole() != null) {
            Long id = loginController.getId().longValue();
            String role = loginController.getRole();

            if ("STUDENT".equalsIgnoreCase(role)) {
                this.studentId = id;
                this.userType = "student";
            } else if ("TEACHER".equalsIgnoreCase(role)) {
                this.teacherId = id;
                this.userType = "teacher";
            }
            teacherController.ListarReservas(loginController.getId().longValue());
            studentController.ListarReservas(loginController.getId().longValue());

            System.out.println("Usuario identificado como " + userType + " con ID: " + id);
        } else {
            System.out.println("ID o rol del usuario no están disponibles desde LoginController.");
        }

        cargarLugares();
    }

    public String obtenerNombreLugar(Reserve reserva) {
        return (reserva.getPlaceId() != null && reserva.getPlaceId().getName() != null) ? reserva.getPlaceId().getName() : "Sin lugar";
    }

    public void guardarReserva() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (reserveDate == null || hourInit == null || hourFinish == null || placeId == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Todos los campos obligatorios deben estar diligenciados.", null));
            context.validationFailed();
            return;
        }

        try {
            Reserve nuevaReserva = new Reserve();
            nuevaReserva.setCode("RES" + String.format("%04d", (int)(Math.random() * 10000)));
            nuevaReserva.setReserveDate(reserveDate);
            nuevaReserva.setHourInit(hourInit);
            nuevaReserva.setHourFinish(hourFinish);
            nuevaReserva.setState("ACTIVO");

            Place lugar = new Place();
            lugar.setId(placeId);
            System.out.println("ID DEL LUGAR:" + lugar);
            nuevaReserva.setPlaceId(lugar);




            if ("student".equals(userType)) {
                Student s = new Student();
                s.setId(studentId);
                nuevaReserva.setStudentId(s); // CAMBIO IMPORTANTE
            } else if ("teacher".equals(userType)) {
                Teacher t = new Teacher();
                t.setId(teacherId);
                nuevaReserva.setTeacherId(t); // CAMBIO IMPORTANTE
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "No se puede guardar la reserva: usuario no identificado.", null));
                return;
            }
            System.out.println("LA RESWERVA NUEVA ES:" + nuevaReserva);
            httpClientService.post(API_URL, nuevaReserva, false);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva guardada correctamente.", null));


            teacherController.ListarReservas(loginController.getId().longValue());
            studentController.ListarReservas(loginController.getId().longValue());

            limpiarFormulario();
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al guardar la reserva.", null));
        }
    }


    public void limpiarFormulario() {
        this.reserveDate = null;
        this.hourInit = null;
        this.hourFinish = null;
        this.placeId = null;
    }

    public List<Place> getListaLugares() {
        System.out.println("getListaLugares() llamado. Lugares: " + listaLugares.size());
        if (listaLugares == null || listaLugares.isEmpty()) {
            cargarLugares();
        }
        return listaLugares;
    }
    public void cargarLugares() {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/place"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                listaLugares = mapper.readValue(response.body(), new TypeReference<List<Place>>() {});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void UploadReserve(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            // Recibe el JSON crudo
            Object json = httpClientService.get(API_URL + "/" + id, false);

            // Deserializa con ObjectMapper a la clase Reserve
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // soporte para fechas
            Reserve reserva = mapper.convertValue(json, Reserve.class);

            this.idReservaSeleccionada = reserva.getId();
            this.reserveDate = reserva.getReserveDate();
            this.hourInit = reserva.getHourInit();
            this.hourFinish = reserva.getHourFinish();

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al cargar la reserva.", null));
        }
    }

    public void prepareUpdate(Long id) {
        UploadReserve(id); // carga los datos en el formulario
    }
    public void UpdateReserve(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (idReservaSeleccionada == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se ha seleccionado ninguna reserva para actualizar.", null));
            return;
        }

        try {
            Reserve reservaActualizada = new Reserve();
            reservaActualizada.setReserveDate(reserveDate);
            reservaActualizada.setHourInit(hourInit);
            reservaActualizada.setHourFinish(hourFinish);

            httpClientService.put(API_URL + "/" + idReservaSeleccionada, reservaActualizada, false);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva actualizada correctamente.", null));

            teacherController.ListarReservas(loginController.getId().longValue());
            studentController.ListarReservas(loginController.getId().longValue());

            limpiarFormulario();
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al actualizar la reserva.", null));
        }
    }

    public String updateReserve(){
        System.out.println("✅ ID recibido directamente: " + idReservaSeleccionada);
        UpdateReserve(idReservaSeleccionada);
        studentController.ListarReservas(loginController.getId().longValue());

        return null;
    }


    public void DeleteReserve(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            httpClientService.delete(API_URL + "/" + id, false);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva eliminada correctamente.", null));


            teacherController.ListarReservas(loginController.getId().longValue());
            studentController.ListarReservas(loginController.getId().longValue());

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al eliminar la reserva.", null));
        }
    }
    public String deleteReserveConParametro(Long id) {
        System.out.println("✅ ID recibido directamente: " + id);
        DeleteReserve(id);
        studentController.ListarReservas(loginController.getId().longValue());

        return null;
    }

}
