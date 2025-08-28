package co.edu.udes.FrontWeb.controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import co.edu.udes.FrontWeb.service.HttpClientService;
import lombok.Data;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

@Named("loginController")
@SessionScoped
@Data
public class LoginController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String API_URL = "http://localhost:8080/api/auth/login";

    @Inject
    private HttpClientService httpClientService;

    private Integer id;
    private String email;
    private String password;
    private String name;
    private String role;

    public String login() {
        try {
            Map<String, String> requestBody = Map.of(
                    "email", email,
                    "password", password
            );
            System.out.println("Attempting login to: " + API_URL);
            System.out.println("With email: " + email);

            Map<String, Object> response = (Map<String, Object>) httpClientService.post(API_URL, requestBody, false);
            System.out.println("Response received: " + response);
            this.name = (String) response.get("name");
            Map<String,Object> roleMap = (Map<String,Object>) response.get("role");
            this.role = roleMap != null ? (String) roleMap.get("name") : null;

            String token = (String) response.get("token");

            FacesContext context = FacesContext.getCurrentInstance();
            context.getExternalContext().getSessionMap().put("authToken", token);
            context.getExternalContext().getSessionMap().put("userName", this.name);
            context.getExternalContext().getSessionMap().put("userRole", this.role);
            setId((Integer) response.get("id"));

            return "home?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Error en el login: " + e.getMessage(), null));
            context.validationFailed();
            return null;
        }
    }
    public String redirigirReserva() {
        if ("TEACHER".equals(role)) {
            return "ReservePage.xhtml?faces-redirect=true";
        } else if ("STUDENT".equals(role)) {
            return "ReserveStudent.xhtml?faces-redirect=true";
        } else {
            return "error.xhtml";
        }
    }
    public String redirigirLibreria(){
        if ("TEACHER".equals(role)) {
            return "loan_library.xhtml?faces-redirect=true";
        } else if ("STUDENT".equals(role)) {
            return "loan_Students.xhtml?faces-redirect=true";
        } else {
            return "error.xhtml";
        }
    }

}