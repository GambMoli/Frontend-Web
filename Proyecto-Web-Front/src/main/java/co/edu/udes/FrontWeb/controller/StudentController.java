package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.Loan;
import co.edu.udes.FrontWeb.model.Reserve;
import co.edu.udes.FrontWeb.model.Student;
import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Named("studentController")
@SessionScoped
public class StudentController implements Serializable {

    @Inject
    private HttpClientService httpClientService;
    @Inject
    private LoginController loginController;

    private List<Reserve> listReserve = new ArrayList<>();
    public List<Loan> listLoans= new ArrayList<>();
    private List<Student> studentList = new ArrayList<>();

    private static final String API_URL = "http://localhost:8080/api/students";

    @PostConstruct
    public void init() {
        System.out.println("Se ejecuta init() de GroupController");
        System.out.println("Desde session: " + loginController.getId());
        System.out.println("Desde loginController.getId(): " + loginController.getId());

        Long id = loginController.getId().longValue(); // o usa getUserIdFromSession()
        if (id != null) {
            ListarReservas(id);
            ListarPrestamos(id);
        } else {
            System.out.println("ID de usuario es null, no se puede listar grupos");
        }
    }

    public void ListarReservas(Long id){
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            listReserve = (List<Reserve>) httpClientService.get(API_URL + "/" + id + "/my_reserve", false);
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
            listLoans = (List<Loan>) httpClientService.get(API_URL + "/" + id + "/my_loans", false);
            context.getExternalContext().getSessionMap().put("ListaLoans", listLoans);

        }catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }
}
