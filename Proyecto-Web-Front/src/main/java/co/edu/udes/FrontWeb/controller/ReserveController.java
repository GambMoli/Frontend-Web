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

        cargarLugares();
        listarReservas();

    }
    public void listarReservas() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            listaReservas = (List<Reserve>) httpClientService.get(API_URL, false);
            context.getExternalContext().getSessionMap().put("listaReservas", listaReservas);
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }

    public String obtenerNombreLugar(Reserve reserva) {
        return (reserva.getPlace() != null && reserva.getPlace().getName() != null) ? reserva.getPlace().getName() : "Sin lugar";
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
            nuevaReserva.setPlace(lugar);

            if ("student".equals(userType)) {
                Student estudiante = new Student();
                estudiante.setId(studentId);
                nuevaReserva.setStudent(estudiante);

            } else {
                Teacher profesor = new Teacher();
                profesor.setId(teacherId);
                nuevaReserva.setTeacher(profesor);
            }

            httpClientService.post(API_URL, nuevaReserva, false);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva guardada correctamente.", null));
            listarReservas();
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
            Reserve reserva = (Reserve) httpClientService.get(API_URL + "/" + id, false);
            this.idReservaSeleccionada = reserva.getId();
            this.reserveDate = reserva.getReserveDate().toString();
            this.hourInit = reserva.getHourInit().toString();
            this.hourFinish = reserva.getHourFinish().toString();
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
            listarReservas();
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
        return null;
    }


    public void DeleteReserve(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            httpClientService.delete(API_URL + "/" + id, false);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva eliminada correctamente.", null));
            listarReservas();
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al eliminar la reserva.", null));
        }
    }
    public String deleteReserveConParametro(Long id) {
        System.out.println("✅ ID recibido directamente: " + id);
        DeleteReserve(id);
        return null;
    }

}
