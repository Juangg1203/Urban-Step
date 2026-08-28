<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:460px;">
    <div class="text-center mb-3">
        <img src="${ctx}/recursos/img/logo.jpg" alt="UrbanStep" class="marca-logo-grande">
    </div>
    <p class="rotulo mb-1">Acceso</p>
    <h1 class="mb-4"><spring:message code="login.titulo" /></h1>

    <c:if test="${param.error != null}">
        <div class="alert alert-danger rounded-0" role="alert">
            <spring:message code="login.error" />
        </div>
    </c:if>

    <form method="post" action="${ctx}/login" class="ficha p-4">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
        <div class="mb-3">
            <label class="rotulo" for="usuario"><spring:message code="login.usuario" /></label>
            <input id="usuario" name="usuario" class="form-control" required autofocus>
        </div>
        <div class="mb-4">
            <label class="rotulo" for="clave"><spring:message code="login.clave" /></label>
            <input id="clave" name="clave" type="password" class="form-control" required>
        </div>
        <button class="btn btn-tinta w-100 py-2"><spring:message code="login.entrar" /></button>
        <p class="small text-muted mt-3 mb-0">
            <spring:message code="login.sinCuenta" /> <a href="${ctx}/registro"><spring:message code="registro.titulo" /></a>. El personal interno se crea
            desde la administracion, no por este formulario.
        </p>
    </form>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
