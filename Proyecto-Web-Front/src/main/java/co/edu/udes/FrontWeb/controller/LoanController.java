package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.Loan;
import co.edu.udes.FrontWeb.model.Material;
import co.edu.udes.FrontWeb.model.Reserve;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("loanController")
@SessionScoped
public class LoanController implements Serializable {
    private Loan loan =new Loan();
    private List<Material> listaMateriales= new ArrayList<>();
    private List<Loan> listaPrestamos= new ArrayList<>();

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

    public void prepareUpdate(Long id) {
        // lógica para cargar el material con ese id y preparar el formulario
    }

    @PostConstruct
    public void init(){
        listLoans();
    }
    public String obtenerNombreMaterial() {
        if (loan != null && loan.getMaterialId() != null) {
            return loan.getMaterialId().getName(); // Asegúrate que getMaterialId() devuelve un Material completo
        }
        return "No seleccionado";
    }
    public Date getLoanDateAsDate() {
        if (loan != null && loan.getLoanDate() != null) {
            return java.util.Date.from(loan.getLoanDate().atZone(ZoneId.systemDefault()).toInstant());
        }
        return null;
    }
    public void PreparedLoan(Long materialId) {
        loan = new Loan();
        // Busca el material por ID
        Material materialSeleccionado = listaMateriales.stream()
                .filter(m -> m.getId() ==materialId)
                .findFirst()
                .orElse(null);
        loan.setMaterialId(materialSeleccionado);
        loan.setCode("LOAN-" +   String.format("%04d", (int)(Math.random() * 10000))); // Código automático); // Generador de código simple
        loan.setLoanDate(LocalDateTime.now());
        loan.setStatus("ACTIVE");
        loan.setReturnState("GOOD");

        // Estudiante o profesor (ejemplo, por login o sesión)
        loan.setStudentId(3L); // o null si no aplica
        loan.setTeacherId(null);
    }
    public void guardarReserva() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(loan);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/reserve"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Reserva exitosa"));
            } else {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error al reservar", null));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    public void deleteMaterial(Long id) {
        // lógica para eliminar el material
    }


}
