package co.edu.udes.FrontWeb.service;

import jakarta.inject.Inject;
import jakarta.faces.context.FacesContext;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class HttpClientService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private transient FacesContext facesContext;

    private final HttpClient client;
    private final ObjectMapper mapper;

    public HttpClientService() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.mapper = new ObjectMapper();
    }

    public Object post(String url, Object body, boolean useAuth) throws Exception {
        return executeRequest("POST", url, body, useAuth);
    }

    public Object get(String url, boolean useAuth) throws Exception {
        return executeRequest("GET", url, null, useAuth);
    }

    public Object put(String url, Object body, boolean useAuth) throws Exception {
        return executeRequest("PUT", url, body, useAuth);
    }

    public Object delete(String url, boolean useAuth) throws Exception {
        return executeRequest("DELETE", url, null, useAuth);
    }

    private Object executeRequest(String method, String url, Object body, boolean useAuth) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json");

        if (useAuth) {
            String token = (String) facesContext.getExternalContext().getSessionMap().get("authToken");
            if (token != null) {
                requestBuilder.header("Authorization", "Bearer " + token);
            }
        }

        if (body != null && !method.equals("GET") && !method.equals("DELETE")) {
            String jsonBody = mapper.writeValueAsString(body);
            requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
        }

        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return mapper.readValue(response.body(), List.class);
            } catch (Exception e) {
                return mapper.readValue(response.body(), Map.class);
            }
        } else {
            throw new RuntimeException("HTTP Error: " + response.statusCode() + " - " + response.body());
        }
    }
}
