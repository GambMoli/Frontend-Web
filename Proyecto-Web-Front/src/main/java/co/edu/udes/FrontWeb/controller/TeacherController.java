package co.edu.udes.FrontWeb.controller;

import co.edu.udes.FrontWeb.model.Group;
import co.edu.udes.FrontWeb.model.Reserve;
import co.edu.udes.FrontWeb.service.HttpClientService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named("teacherController")
@SessionScoped
public class TeacherController implements Serializable {
    @Inject
    private HttpClientService httpClientService;
    @Inject
    private LoginController loginController;


    private List<Group> listaGrupos = new ArrayList<>();
    private List<Reserve> listReserve = new ArrayList<>();
    private Group selectedGroup;
    private Long idGrupoSeleccionado;

    private static final String API_URL = "http://localhost:8080/api/teachers";
    //Getters y setters


    public Long getIdGrupoSeleccionado() {
        return idGrupoSeleccionado;
    }

    public void setIdGrupoSeleccionado(Long idGrupoSeleccionado) {
        this.idGrupoSeleccionado = idGrupoSeleccionado;
    }

    public Group getSelectedGroup() {
        return selectedGroup;
    }

    public void setSelectedGroup(Group selectedGroup) {
        this.selectedGroup = selectedGroup;
    }

    public List<Group> getListaGrupos() {
        return listaGrupos;
    }

    public void setListaGrupos(List<Group> listaGrupos) {
        this.listaGrupos = listaGrupos;
    }

    public List<Reserve> getListReserve() {
        return listReserve;
    }

    public void setListReserve(List<Reserve> listReserve) {
        this.listReserve = listReserve;
    }

    @PostConstruct
    public void init() {
        System.out.println("Se ejecuta init() de GroupController");
        System.out.println("Desde session: " + loginController.getId());
        System.out.println("Desde loginController.getId(): " + loginController.getId());

        Long id = loginController.getId().longValue(); // o usa getUserIdFromSession()
        if (id != null) {
            ListarGrupos(id);
            ListarReservas(id);
        } else {
            System.out.println("ID de usuario es null, no se puede listar grupos");
        }
    }
    public void ListarGrupos(Long id) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            listaGrupos = (List<Group>) httpClientService.get(API_URL + "/" + id + "/myGroups", false);
            context.getExternalContext().getSessionMap().put("ListaGrupos", listaGrupos);

        } catch (Exception e) {
            e.printStackTrace();
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error de conexión con el servidor.", null));
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
    public String verEstudiantesPorGrupo() {
        // Podrías guardar el ID en sesión si quieres usarlo en otra página
        FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("idGrupo", idGrupoSeleccionado);

        // Redirige a la página donde mostrarás los estudiantes
        return "studentsByGroup.xhtml?faces-redirect=true";
    }
}
