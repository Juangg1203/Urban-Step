<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:640px;">
    <p class="rotulo mb-1">Acceso restringido</p>
    <h1 class="mb-3">Tu rol no puede abrir esta pagina</h1>
    <p>Esto no es un error del sistema: es la politica de acceso funcionando. Cada rol ve unicamente los
        niveles de informacion que necesita para su trabajo, y el intento quedo registrado en la auditoria.</p>
    <a href="${ctx}/" class="btn btn-tinta">Volver al inicio</a>
    <a href="${ctx}/politica-datos" class="btn btn-contorno">Ver la politica</a>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
