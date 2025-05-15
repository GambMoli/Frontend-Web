<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="../components/Header/header.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/StyleHome.css">

    <body>

       <div class="info-row" style="display: flex; margin-top: 40px;">
       <%@ include file="../components/ButtonsHome/Buttons.jsp" %>
           <!-- Información Académica -->
           <div class="academic-info">
               <h4>Información Académica</h4>
               <table class="table-container">
                   <tr>
                       <th>Concepto</th>
                       <th>Valor</th>
                   </tr>
                   <tr>
                       <td>Promedio</td>
                       <td>3,86</td>
                   </tr>
                   <tr>
                       <td>Semestre</td>
                       <td>5</td>
                   </tr>
                   <tr>
                       <td>Activo académico</td>
                       <td class="checkmark">&#10003;</td>
                   </tr>
               </table>
                <div class="news-section">
                                  <span>&#128240; Noticias</span>
                                  <span>0</span>
                              </div>
                              <div class="news-content">
                                  No hay noticias nuevas.
                              </div>
           </div>

           <!-- Noticias -->
           <div class="news-section-container">

           </div>
       </div>
    </body>