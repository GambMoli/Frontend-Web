package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.Place;
import co.edu.udes.FrontWeb.model.Reserve;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Named("reserveController")
@SessionScoped
public class ReserveController implements Serializable {

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
    private static final String API_URL = "http://localhost:8081/api/reserve";

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
    public void listarReservas(){
        FacesContext context = FacesContext.getCurrentInstance();
        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/reserve"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                listaReservas = mapper.readValue(response.body(), new TypeReference<List<Reserve>>() {});
            }
            context.getExternalContext().getSessionMap().put("listaReservas", listaReservas);

        }catch (Exception e) {
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
            String code = "RES" + String.format("%04d", (int)(Math.random() * 10000)); // Código automático
            String state = "ACTIVO"; // Estado fijo

            String jsonBody;


            if ("student".equals(userType)) {
                System.out.println("Place ID antes de enviar: " + placeId);

                jsonBody = String.format("""
                    {
                        "code": "%s",
                        "reserveDate": "%s",
                        "hourInit": "%s",
                        "hourFinish": "%s",
                        "state": "%s",
                        "placeId": { "id": %d },
                        "studentId": %d 
                    }
                    """, code, reserveDate, hourInit, hourFinish, state, placeId, studentId);
            } else {
                System.out.println("Place ID antes de enviar: " + placeId);

                jsonBody = String.format("""
                    {
                        "code": "%s",
                        "reserveDate": "%s",
                        "hourInit": "%s",
                        "hourFinish": "%s",
                        "state": "%s",
                        "placeId": { "id": %d },
                        "teacherId": %d}
                    }
                    """, code, reserveDate, hourInit, hourFinish, state, placeId, teacherId);
            }

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Respuesta del servidor: " + response.body()); // Debug

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Reserva guardada correctamente.", null));
                listarReservas();
                limpiarFormulario();
                System.out.println("Reserva enviada con placeId: " + placeId);
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al guardar la reserva. Código: " + response.statusCode(), null));
            }

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
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
                    .uri(URI.create("http://localhost:8081/api/place"))
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
    public void cargarReserva() {
        if (idReservaSeleccionada == null) return;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/reserve/" + idReservaSeleccionada))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                Reserve reserva = mapper.readValue(response.body(), Reserve.class);

                this.reserveDate = reserva.getReserveDate().toString();
                this.hourInit = reserva.getHourInit().toString();
                this.hourFinish = reserva.getHourFinish().toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void actualizarReserva() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (idReservaSeleccionada == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error: No se ha seleccionado ninguna reserva para actualizar.", null));
            return;
        }

        try {
            String jsonBody = String.format("""
        {
            "reserveDate": "%s",
            "hourInit": "%s",
            "hourFinish": "%s"
        }
        """, reserveDate, hourInit, hourFinish);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/reserve/" + idReservaSeleccionada))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());


            if (response.statusCode() == 200 || response.statusCode() == 204) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Reserva actualizada correctamente.", null));
                listarReservas(); // Refrescar la lista
                limpiarFormulario();
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al actualizar la reserva. Código: " + response.statusCode(), null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }

    public void eliminarReserva(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8081/api/reserve/" + id))
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 204) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "Reserva eliminada correctamente.", null));
                listarReservas();  // Actualizar la lista después de eliminar
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error al eliminar la reserva. Código: " + response.statusCode(), null));
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }

}
