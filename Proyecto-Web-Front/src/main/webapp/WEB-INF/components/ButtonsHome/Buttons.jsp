<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

<!-- Font Awesome for icons -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<!-- Estilos personalizados -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/StyleButton.css">

<!-- Bootstrap JS y dependencias -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- Buttons -->
<div class="ContainerHome">
    <div class="ContenedorBotones">
        <div class= "tresBotones">
            <a href="${pageContext.request.contextPath}/views/chats/chats.jsp" class="btn btn-primary">
                         <div>
                            <span><i class="fa-brands fa-rocketchat"></i></span>
                         </div>
                            Mensajeria
                        </a>

                        <a href="${pageContext.request.contextPath}/views/chats/chats.jsp" class="btn btn-primary">
                         <div>
                            <span><i class="fa-solid fa-briefcase"></i></span>
                         </div>
                            Matricula
                        </a>

                        <a href="${pageContext.request.contextPath}/views/chats/chats.jsp" class="btn btn-primary">
                         <div>
                            <span><i class="fa-solid fa-pen-to-square"></i></span>
                         </div>
                            Notas
                        </a>
        </div>
        <div class="dosBotones">
            <a href="${pageContext.request.contextPath}/views/chats/chats.jsp" class="btn btn-primary">
                             <div>
                                <span><i class="fa-solid fa-calendar-days"></i></span>
                             </div>
                                Horario
                        </a>

                        <a href="${pageContext.request.contextPath}/views/chats/chats.jsp" class="btn btn-primary">
                             <div>
                                <span><i class="fa-solid fa-user"></i></span>
                             </div>
                                Usuarios
                        </a>
        </div>

    </div>
</div>