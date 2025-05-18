package co.edu.udes.FrontWeb.controller;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Named("loginController")
@SessionScoped
public class LoginController implements Serializable {

    private String email;
    private String password;
    private String token;
    private String name;
    private String role;

    private static final String API_URL = "http://localhost:8080/api/auth/login";

    // Getters y setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getToken() { return token; }
    public String getName() { return name; }
    public String getRole() { return role; }

    public String login() {
        try {
            System.out.println("Entro al login");
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            String jsonBody = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", email, password);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response);
            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                Map<String,Object> result = mapper.readValue(response.body(), Map.class);

                this.token = (String) result.get("token");
                this.name = (String) result.get("name");
                Map<String,Object> roleMap = (Map<String,Object>) result.get("role");
                this.role = roleMap != null ? (String) roleMap.get("name") : null;

                System.out.println(token + "token");
                System.out.println(name + "name");

                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("authToken", this.token);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userName", this.name);
                FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("userRole", this.role);

                return "hola?faces-redirect=true";

            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Credenciales incorrectas o error del sistema", null));
                context.validationFailed();
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error de conexión con el servidor", null));
            context.validationFailed();
            return null;
        }
    }
}
