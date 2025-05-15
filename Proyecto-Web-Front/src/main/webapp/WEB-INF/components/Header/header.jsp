<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!-- Bootstrap CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">

<!-- Font Awesome for icons -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

<!-- Estilos personalizados -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/headerStyles.css">

<!-- Bootstrap JS y dependencias -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<!-- Header -->
<div class="udes-header">
    <div class="container-fluid px-0">
        <!-- Navigation bar -->
        <nav class="navbar navbar-expand navbar-light bg-light border-bottom">
            <div class="container-fluid">
                <!-- Logo + Left side navigation -->
                <div class="d-flex align-items-center">
                    <a class="navbar-brand me-3" href="${pageContext.request.contextPath}/views/HomePage/Home.jsp">
                        <img src="${pageContext.request.contextPath}/Assets/logo2.png" alt="Logo UDES" class="udes-logo">
                    </a>
                    <div class="navbar-nav">
                        <a class="nav-link" href="#">Trámites Académicos</a>
                        <a class="nav-link" href="#">Record de notas</a>
                        <a class="nav-link" href="#">Plan de estudio</a>
                        <a class="nav-link" href="${pageContext.request.contextPath}/views/enrollment/enroll.jsp">Matrícula</a>
                        <a class="nav-link" href="#">Horario</a>
                        <a class="nav-link" href="#">Notas</a>
                    </div>
                </div>

                <!-- Right side navigation -->
                <div class="d-flex align-items-center">
                    <a href="#" class="nav-icon me-3"><i class="fas fa-bell"></i></a>
                    <div class="dropdown">
                        <a class="dropdown-toggle nav-link user-dropdown" href="#" role="button" data-bs-toggle="dropdown">
                            <i class="fas fa-user-circle"></i> LUIS ALEJANDRO VERGEL IRLET
                        </a>
                        <ul class="dropdown-menu dropdown-menu-end">
                            <li><a class="dropdown-item" href="${pageContext.request.contextPath}/views/user_config/profile.jsp">Perfil</a></li>
                            <li><a class="dropdown-item" href="#">Cerrar sesión</a></li>
                        </ul>
                    </div>
                </div>
            </div>
        </nav>
    </div>
</div>