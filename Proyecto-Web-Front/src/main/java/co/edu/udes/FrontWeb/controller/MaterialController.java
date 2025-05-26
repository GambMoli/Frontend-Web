package co.edu.udes.FrontWeb.controller;

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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Named("materialController")
@SessionScoped
public class MaterialController {
private List<Material> listaMateriales= new ArrayList<>();

    public List<Material> getlistaMateriales() {
        return listaMateriales;
    }

    public void setlistaMateriales(List<Material> listaReservas) {
        this.listaMateriales = listaMateriales;
    }


    @PostConstruct
    public void init() {
        listMaterials();
    }
    public void listMaterials(){
        FacesContext context = FacesContext.getCurrentInstance();
        try{
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/material"))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());
                listaMateriales = mapper.readValue(response.body(), new TypeReference<List<Material>>() {});
            }
            context.getExternalContext().getSessionMap().put("listaMaterialess", listaMateriales);

        }catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
        }
    }
}
