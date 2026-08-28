<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="${pageContext.response.locale.language}">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="theme-color" content="#0B0E14">
    <link rel="icon" type="image/jpeg" href="${ctx}/recursos/img/logo.jpg">
    <title>${empty titulo ? 'UrbanStep' : titulo} | UrbanStep</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Space+Grotesk:wght@400;500;700&family=Archivo+Black&family=JetBrains+Mono:wght@400;700&display=swap" rel="stylesheet">
    <link href="${ctx}/recursos/css/estilos.css" rel="stylesheet">
</head>
<body>

<nav class="navbar navbar-expand-lg barra">
    <div class="container">
        <a class="navbar-brand d-flex align-items-center gap-2" href="${ctx}/">
            <img src="${ctx}/recursos/img/logo.jpg" alt="UrbanStep" class="marca-logo">
            Urban<span>Step</span>
        </a>
        <button class="navbar-toggler border-0 text-white" type="button"
                data-bs-toggle="collapse" data-bs-target="#menu" aria-controls="menu"
                aria-expanded="false" aria-label="Abrir menu">
            <i class="bi bi-list fs-3"></i>
        </button>
        <div class="collapse navbar-collapse" id="menu">
            <ul class="navbar-nav me-auto">
                <li class="nav-item"><a class="nav-link" href="${ctx}/"><spring:message code="nav.inicio" /></a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/catalogo"><spring:message code="nav.catalogo" /></a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/catalogo?linea=ROPA"><spring:message code="nav.ropa" /></a></li>
                <li class="nav-item"><a class="nav-link" href="${ctx}/catalogo?linea=CALZADO"><spring:message code="nav.calzado" /></a></li>
            </ul>
            <ul class="navbar-nav">
                <sec:authorize access="isAnonymous()">
                    <li class="nav-item"><a class="nav-link" href="${ctx}/login"><spring:message code="nav.login" /></a></li>
                    <li class="nav-item"><a class="nav-link fw-bold" href="${ctx}/registro"><spring:message code="nav.registro" /></a></li>
                </sec:authorize>
                <sec:authorize access="hasRole('CLIENTE')">
                    <li class="nav-item"><a class="nav-link" href="${ctx}/pedidos"><spring:message code="nav.pedidos" /></a></li>
                    <li class="nav-item"><a class="nav-link" href="${ctx}/mi-cuenta"><spring:message code="nav.micuenta" /></a></li>
                </sec:authorize>
                <sec:authorize access="hasAnyRole('ADMIN','EMPLEADO','JEFE')">
                    <li class="nav-item">
                        <a class="nav-link" href="${ctx}/panel"><spring:message code="nav.panel" />
                            <c:if test="${avisosSinLeer > 0}">
                                <span class="globo">${avisosSinLeer}</span>
                            </c:if>
                        </a>
                    </li>
                </sec:authorize>
                <li class="nav-item">
                    <a class="nav-link" href="${ctx}/carrito"><spring:message code="nav.carrito" />
                        <c:if test="${unidadesCarrito > 0}">
                            <span class="globo">${unidadesCarrito}</span>
                        </c:if>
                    </a>
                </li>
                <%-- El cambio de idioma conserva la pagina pero no los filtros
                     de la URL: ?lang=xx reemplaza la cadena de consulta. --%>
                <li class="nav-item selector-idioma">
                    <a class="nav-link d-inline p-0" href="?lang=es" title="Espanol">ES</a>
                    <span class="separador-idioma">/</span>
                    <a class="nav-link d-inline p-0" href="?lang=en" title="English">EN</a>
                </li>
                <sec:authorize access="isAuthenticated()">
                    <li class="nav-item">
                        <form action="${ctx}/salir" method="post" class="d-inline">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                            <button class="nav-link btn btn-link" type="submit"><spring:message code="nav.logout" /></button>
                        </form>
                    </li>
                </sec:authorize>
            </ul>
        </div>
    </div>
</nav>

<c:if test="${not empty mensaje}">
    <div class="container mt-3">
        <div class="alert alert-success border-0 rounded-0 mb-0" role="status">${mensaje}</div>
    </div>
</c:if>
<c:if test="${not empty error}">
    <div class="container mt-3">
        <div class="alert alert-danger border-0 rounded-0 mb-0" role="alert">${error}</div>
    </div>
</c:if>
