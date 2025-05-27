package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.*;
import co.edu.udes.FrontWeb.service.HttpClientService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Named("loanController")
@SessionScoped
public class LoanController implements Serializable {


    @Inject
    private HttpClientService httpClientService;
    private static final String API_URL = "http://localhost:8080/api/loan";

    private Loan loan =new Loan();

    private List<Material> listaMateriales= new ArrayList<>();
    private List<Loan> listaPrestamos= new ArrayList<>();

    private String code;
    private String loanDate;
    private LocalDate actualReturnDate;
    private LocalDate deadline;
    private Long studentId;
    private Long teacherId;
    private Long materialId;
    private String status;
    private String returnState;
    private String userType;
    private Long idReservaSeleccionada;

    public Long getIdReservaSeleccionada() {
        return idReservaSeleccionada;
    }

    public void setIdReservaSeleccionada(Long idReservaSeleccionada) {
        this.idReservaSeleccionada = idReservaSeleccionada;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReturnState() {
        return returnState;
    }

    public void setReturnState(String returnState) {
        this.returnState = returnState;
    }

    public String getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(String loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
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

    public List<Loan> getListaPrestamos() {
        return listaPrestamos;
    }

    public void setListaPrestamos(List<Loan> listaPrestamos) {
        this.listaPrestamos = listaPrestamos;
    }

    public List<Material> getListaMateriales() {
        return listaMateriales;
    }

    public void setListaMateriales(List<Material> listaMateriales) {
        this.listaMateriales = listaMateriales;
    }

    public Loan getLoan() {
        return loan;
    }

    public void setLoan(Loan loan) {
        this.loan = loan;
    }


    @PostConstruct
    public void init(){
        listLoans();
    }



    public String uploadLoan(Long materialId) {
        this.loan = new Loan();
        this.materialId = materialId;
        System.out.println("Material seleccionado: " + materialId);
        return null;
    }


    public void saveLoan() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            System.out.println("Entrando a saveLoan con materialId = " + materialId + " y fecha = " + loanDate);

            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
            LocalDate parsedLoanDate = ZonedDateTime.parse(loanDate, inputFormatter).toLocalDate();

            String formattedLoanDate = parsedLoanDate.toString(); // "2025-05-30"
            String formattedDeadline = parsedLoanDate.plusDays(15).toString(); // "2025-06-14"
            Loan newLoan = new Loan();




            newLoan.setMaterialId(materialId);

            newLoan.setLoanDate(formattedLoanDate);
            newLoan.setDeadline(formattedDeadline); // Fecha límite automática
            newLoan.setCode("Loan" + String.format("%04d", (int)(Math.random() * 10000)));
            newLoan.setReturnState("GOOD");
            newLoan.setStatus("ACTIVE");

            if ("student".equals(userType)) {
                Student estudiante = new Student();
                estudiante.setId(studentId);
                newLoan.setStudentId(estudiante.getId());
            } else {
                Teacher profesor = new Teacher();
                profesor.setId(teacherId);
                newLoan.setTeacherId(profesor.getId());
            }

            httpClientService.post(API_URL, newLoan, false);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva guardada correctamente.", null));
            listLoans();

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al guardar la reserva.", null));
        }
    }




    public String obtenerNombreMaterial(Loan loan) {
        return loan.getMaterial() != null ?
                loan.getMaterial().getName() :
                "Material no encontrado";
    }

    public void listLoans(){
        FacesContext context = FacesContext.getCurrentInstance();
        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/loan"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                listaPrestamos = mapper.readValue(response.body(), new TypeReference<List<Loan>>() {});
            }
            context.getExternalContext().getSessionMap().put("ListaPrestamos", listaPrestamos);

        }catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }
    public void UploadLoan(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Object response = httpClientService.get(API_URL + "/" + id, false);

            // Serializar primero a JSON y luego convertir a Loan
            ObjectMapper mapper = new ObjectMapper();
            Loan loan = mapper.convertValue(response, Loan.class);

            this.loanDate = loan.getLoanDate();
            this.idReservaSeleccionada = loan.getId();

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }
    public void prepareUpdate(Long id) {
        UploadLoan(id); // carga los datos en el formulario
    }

    public void UpdateLoan(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (idReservaSeleccionada == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "No se ha seleccionado ninguna reserva para actualizar.", null));
            return;
        }

        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate parsedLoanDate = LocalDate.parse(loanDate, inputFormatter);

            String formattedLoanDate = parsedLoanDate.toString(); // "2025-05-30"
            String formattedDeadline = parsedLoanDate.plusDays(15).toString(); // "2025-06-14"

            Loan loanUpdate = new Loan();
            loanUpdate.setLoanDate(formattedLoanDate);
            loanUpdate.setDeadline(formattedDeadline);

            httpClientService.put(API_URL + "/" + idReservaSeleccionada, loanUpdate, false);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva actualizada correctamente.", null));
            listLoans();

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al actualizar la reserva.", null));
        }
    }

    public String updateLoan(){
        System.out.println("✅ ID recibido directamente: " + idReservaSeleccionada);
        UpdateLoan(idReservaSeleccionada);
        return null;
    }

    public void deleteLoan(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            httpClientService.delete(API_URL + "/" + id, false);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Reserva eliminada correctamente.", null));
            listLoans();
        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error al eliminar la reserva.", null));
        }
    }
    public String deleteLoanWithParams(Long id) {
        System.out.println("✅ ID recibido directamente: " + id);
        deleteLoan(id);
        return null;
    }


}
