package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Named("chatController")
@SessionScoped
@Data
public class ChatController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String API_BASE_URL = "http://localhost:8080/api";

    @Inject
    private HttpClientService httpClientService;

    @Inject
    private UserController userController;

    @Inject
    private LoginController loginController;

    // Variables para búsqueda
    private String searchQuery;
    private List<Map<String, Object>> searchResults = new ArrayList<>();

    // Variables para chat actual
    private Map<String, Object> currentChat;
    private List<Map<String, Object>> currentMessages = new ArrayList<>();
    private Integer currentChatId;

    // Variables para nuevo mensaje
    private String newMessageTitle = "Respuesta";
    private String newMessageBody;

    // Variables para contactos/chats existentes
    private List<Map<String, Object>> userChats = new ArrayList<>();

    @PostConstruct
    public void init() {
        System.out.println("Inicializando ChatController...");
        loadUserChats();
    }

    public void searchUsers() {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            searchResults.clear();
            return;
        }

        try {
            String url = API_BASE_URL + "/users/search?query=" + searchQuery;
            System.out.println("Searching users with URL: " + url);

            Object response = httpClientService.get(url, true);

            if (response instanceof List) {
                searchResults = (List<Map<String, Object>>) response;
            } else if (response instanceof Map) {
                searchResults = new ArrayList<>();
                searchResults.add((Map<String, Object>) response);
            } else {
                searchResults = new ArrayList<>();
            }

            System.out.println("Search results: " + searchResults.size() + " users found");

        } catch (Exception e) {
            e.printStackTrace();
            searchResults.clear();
            addErrorMessage("Error al buscar usuarios: " + e.getMessage());
        }
    }

    public void startChat(Integer selectedUserId) {
        try {
            System.out.println("Attempting to start chat with user ID: " + selectedUserId);

            Integer currentUserId = getCurrentUserId();
            System.out.println("Current user ID: " + currentUserId);

            if (currentUserId == null) {
                addErrorMessage("Error: Usuario no identificado");
                return;
            }

            // Crear cuerpo de la solicitud
            Map<String, Integer> requestBody = Map.of(
                    "participant1Id", currentUserId,  // Usuario logeado
                    "participant2Id", selectedUserId // Usuario seleccionado
            );

            // Llamar al endpoint para crear/obtener el chat
            String url = API_BASE_URL + "/chats";
            System.out.println("Creating chat with URL: " + url);
            System.out.println("Request body: " + requestBody);

            // Enviar solicitud
            Map<String, Object> response = (Map<String, Object>) httpClientService.post(url, requestBody, true);

            // Guardar información del chat
            currentChat = response;
            currentChatId = (Integer) response.get("id");
            System.out.println("Chat created with ID: " + currentChatId);

            // Limpiar búsqueda
            searchQuery = "";
            searchResults.clear();

            // Cargar mensajes del chat
            loadMessages();

            // Recargar lista de chats para mostrar el nuevo
            loadUserChats();

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error al iniciar chat: " + e.getMessage());
        }
    }

    public void selectChat(Integer chatId) {
        try {
            System.out.println("Selecting chat with ID: " + chatId);

            // Buscar el chat en la lista de chats del usuario
            Map<String, Object> selectedChat = null;
            for (Map<String, Object> chat : userChats) {
                if (chatId.equals(chat.get("id"))) {
                    selectedChat = chat;
                    break;
                }
            }

            if (selectedChat != null) {
                currentChat = selectedChat;
                currentChatId = chatId;

                // Cargar mensajes del chat seleccionado
                loadMessages();

                System.out.println("Chat selected and messages loaded");
            } else {
                addErrorMessage("Chat no encontrado");
            }

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error al seleccionar chat: " + e.getMessage());
        }
    }

    public void loadChat(Integer chatId) {
        try {
            String url = API_BASE_URL + "/chats/" + chatId;
            System.out.println("Loading chat with URL: " + url);

            Map<String, Object> response = (Map<String, Object>) httpClientService.get(url, true);
            currentChat = response;
            currentChatId = chatId;

            loadMessages();

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error al cargar chat: " + e.getMessage());
        }
    }

    public void loadMessages() {
        if (currentChatId == null) {
            System.out.println("No chat selected, cannot load messages");
            return;
        }

        try {
            String url = API_BASE_URL + "/messages/chat/" + currentChatId;
            System.out.println("Loading messages with URL: " + url);

            Object response = httpClientService.get(url, true);

            if (response instanceof List) {
                currentMessages = (List<Map<String, Object>>) response;
                // Ordenar mensajes por fecha (más recientes al final)
                currentMessages.sort((m1, m2) -> {
                    String date1 = (String) m1.get("date");
                    String date2 = (String) m2.get("date");
                    if (date1 == null || date2 == null) return 0;
                    return date1.compareTo(date2);
                });
            } else {
                currentMessages = new ArrayList<>();
            }

            System.out.println("Messages loaded: " + currentMessages.size());

        } catch (Exception e) {
            e.printStackTrace();
            currentMessages.clear();
            addErrorMessage("Error al cargar mensajes: " + e.getMessage());
        }
    }

    public void sendMessage() {
        System.out.println("=== INICIO sendMessage() ===");
        System.out.println("newMessageBody: '" + newMessageBody + "'");
        System.out.println("currentChatId: " + currentChatId);

        if (newMessageBody == null || newMessageBody.trim().isEmpty()) {
            System.out.println("ERROR: Mensaje vacío");
            addErrorMessage("El mensaje no puede estar vacío");
            return;
        }

        if (currentChatId == null) {
            System.out.println("ERROR: No hay chat seleccionado");
            addErrorMessage("No hay chat seleccionado");
            return;
        }

        try {
            Integer currentUserId = getCurrentUserId();
            System.out.println("currentUserId: " + currentUserId);

            if (currentUserId == null) {
                System.out.println("ERROR: Usuario no identificado");
                addErrorMessage("Error: Usuario no identificado");
                return;
            }

            Map<String, Object> requestBody = Map.of(
                    "senderId", currentUserId,
                    "chatId", currentChatId,
                    "title", newMessageTitle != null ? newMessageTitle : "Mensaje",
                    "body", newMessageBody.trim()
            );

            System.out.println("Request body: " + requestBody);

            String url = API_BASE_URL + "/messages";
            System.out.println("Sending message with URL: " + url);

            Object response = httpClientService.post(url, requestBody, true);
            System.out.println("Message sent successfully: " + response);

            // Limpiar campo de mensaje y recargar
            newMessageBody = "";
            loadMessages();

            System.out.println("=== FIN sendMessage() ===");

        } catch (Exception e) {
            System.out.println("ERROR en sendMessage(): " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Error al enviar mensaje: " + e.getMessage());
        }
    }

    public void loadUserChats() {
        try {
            Integer currentUserId = getCurrentUserId();
            if (currentUserId == null) {
                System.out.println("No user ID found, cannot load chats");
                return;
            }

            String url = API_BASE_URL + "/chats/student/" + currentUserId;
            System.out.println("Loading user chats with URL: " + url);

            Object response = httpClientService.get(url, true);

            if (response instanceof List) {
                userChats = (List<Map<String, Object>>) response;

                // Enriquecer cada chat con información adicional
                for (Map<String, Object> chat : userChats) {
                    enrichChatInfo(chat, currentUserId);
                }

                System.out.println("User chats loaded: " + userChats.size());
            } else {
                userChats = new ArrayList<>();
                System.out.println("No chats found or invalid response format");
            }

        } catch (Exception e) {
            e.printStackTrace();
            userChats = new ArrayList<>();
            System.out.println("Error loading user chats: " + e.getMessage());
        }
    }

    private void enrichChatInfo(Map<String, Object> chat, Integer currentUserId) {
        try {
            Integer participant1Id = (Integer) chat.get("participant1Id");
            Integer participant2Id = (Integer) chat.get("participant2Id");

            // Determinar cuál es el otro participante
            Integer otherParticipantId = currentUserId.equals(participant1Id) ? participant2Id : participant1Id;

            // Verificar si ya tenemos la información del nombre
            String participant1Name = (String) chat.get("participant1Name");
            String participant2Name = (String) chat.get("participant2Name");

            if (currentUserId.equals(participant1Id) && participant2Name != null) {
                // El usuario actual es participant1, usar el nombre de participant2
                chat.put("otherParticipantName", participant2Name);
                chat.put("otherParticipantId", participant2Id);
            } else if (currentUserId.equals(participant2Id) && participant1Name != null) {
                // El usuario actual es participant2, usar el nombre de participant1
                chat.put("otherParticipantName", participant1Name);
                chat.put("otherParticipantId", participant1Id);
            } else {
                // Si no tenemos la información, hacer una llamada adicional
                String userUrl = API_BASE_URL + "/users/" + otherParticipantId;
                Map<String, Object> otherUser = (Map<String, Object>) httpClientService.get(userUrl, true);

                chat.put("otherParticipantName", otherUser.get("name"));
                chat.put("otherParticipantEmail", otherUser.get("email"));
                chat.put("otherParticipantId", otherParticipantId);
            }

        } catch (Exception e) {
            System.out.println("Error enriching chat info: " + e.getMessage());
            e.printStackTrace();
            chat.put("otherParticipantName", "Usuario desconocido");
        }
    }

    private Integer getCurrentUserId() {
        // Intentar obtener del LoginController primero
        if (loginController != null && loginController.getId() != null) {
            try {
                return Integer.valueOf(loginController.getId());
            } catch (NumberFormatException e) {
                System.out.println("Error converting loginController ID to Integer: " + e.getMessage());
            }
        }

        // Intentar obtener del UserController
        if (userController != null && userController.getUser() != null) {
            return userController.getUser().getId();
        }

        // Alternativa: obtener de la sesión
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            Object userId = context.getExternalContext().getSessionMap().get("userId");
            if (userId instanceof Integer) {
                return (Integer) userId;
            }
            if (userId instanceof String) {
                return Integer.valueOf((String) userId);
            }
        } catch (Exception e) {
            System.out.println("Error getting user ID from session: " + e.getMessage());
        }

        System.out.println("Could not determine current user ID");
        return null;
    }

    public String getOtherParticipantName() {
        if (currentChat == null) return "";

        // Primero intentar obtener de la información enriquecida
        String name = (String) currentChat.get("otherParticipantName");
        if (name != null && !name.equals("Usuario desconocido")) {
            return name;
        }

        // Si no está disponible, intentar determinar manualmente
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) return "";

        Integer participant1Id = (Integer) currentChat.get("participant1Id");
        Integer participant2Id = (Integer) currentChat.get("participant2Id");
        String participant1Name = (String) currentChat.get("participant1Name");
        String participant2Name = (String) currentChat.get("participant2Name");

        if (currentUserId.equals(participant1Id)) {
            return participant2Name != null ? participant2Name : "Usuario";
        } else {
            return participant1Name != null ? participant1Name : "Usuario";
        }
    }

    public boolean isCurrentUserMessage(Map<String, Object> message) {
        Integer currentUserId = getCurrentUserId();
        Integer senderId = (Integer) message.get("senderId");
        return currentUserId != null && currentUserId.equals(senderId);
    }

    private void addErrorMessage(String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
        }
        System.out.println("Error: " + message);
    }

    // Getters adicionales
    public boolean getIsChatSelected() {
        return currentChatId != null;
    }

    public boolean getHasSearchResults() {
        return searchResults != null && !searchResults.isEmpty();
    }

    public boolean getHasMessages() {
        return currentMessages != null && !currentMessages.isEmpty();
    }

    public boolean getHasUserChats() {
        return userChats != null && !userChats.isEmpty();
    }

    public boolean isChatActive(Integer chatId) {
        return currentChatId != null && currentChatId.equals(chatId);
    }

    public void handleKeyPress(jakarta.faces.event.AjaxBehaviorEvent event) {
        // Este método maneja el evento de tecla presionada
        // La lógica real está en JavaScript, este método solo existe para evitar errores
    }
}