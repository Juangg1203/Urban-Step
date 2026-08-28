<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<section class="container py-5" style="max-width:820px;">
    <div class="text-center mb-3">
        <img src="${ctx}/recursos/img/logo.jpg" alt="UrbanStep" class="marca-logo-grande">
    </div>
    <p class="rotulo mb-1">Cuenta nueva</p>
    <h1 class="mb-2">Crear cuenta</h1>
    <p class="text-muted mb-4">Solo pedimos lo necesario. Los campos marcados como privados se guardan
        cifrados y los sensibles ni siquiera se piden aqui: los agregas despues si quieres.</p>

    <form:form method="post" modelAttribute="registroForm" cssClass="ficha p-4">

        <p class="rotulo mb-2">Acceso</p>
        <div class="row g-3 mb-4">
            <div class="col-md-4">
                <label class="form-label small" for="nombreUsuario">Usuario</label>
                <form:input path="nombreUsuario" id="nombreUsuario" cssClass="form-control" />
                <form:errors path="nombreUsuario" cssClass="text-danger small" />
            </div>
            <div class="col-md-4">
                <label class="form-label small" for="correo">Correo</label>
                <form:input path="correo" id="correo" type="email" cssClass="form-control" />
                <form:errors path="correo" cssClass="text-danger small" />
            </div>
            <div class="col-md-4">
                <div class="d-flex justify-content-between align-items-baseline">
                    <label class="form-label small mb-0" for="clave">Clave</label>
                    <span>
                        <button type="button" id="sugerirClave" class="btn btn-link btn-sm p-0 dato">Sugerir</button>
                        <span class="text-muted dato">&middot;</span>
                        <button type="button" id="verClave" class="btn btn-link btn-sm p-0 dato">Ver</button>
                    </span>
                </div>
                <form:password path="clave" id="clave" cssClass="form-control"
                               autocomplete="new-password" />
                <form:errors path="clave" cssClass="text-danger small" />

                <div class="medidor mt-2">
                    <div id="barraClave" class="medidor-relleno"></div>
                </div>
                <span id="textoClave" class="dato medidor-texto"></span>
                <ul id="avisosClave" class="avisos-clave"></ul>
                <div id="claveSugerida" class="clave-sugerida" hidden></div>
            </div>
        </div>

        <div class="d-flex align-items-center gap-2 mb-2">
            
            <span class="small text-muted">Visible para el equipo que te atiende</span>
        </div>
        <div class="row g-3 mb-4">
            <div class="col-md-6">
                <label class="form-label small" for="nombres">Nombres</label>
                <form:input path="nombres" id="nombres" cssClass="form-control" />
                <form:errors path="nombres" cssClass="text-danger small" />
            </div>
            <div class="col-md-6">
                <label class="form-label small" for="apellidos">Apellidos</label>
                <form:input path="apellidos" id="apellidos" cssClass="form-control" />
                <form:errors path="apellidos" cssClass="text-danger small" />
            </div>
            <div class="col-md-6">
                <label class="form-label small" for="departamento">Departamento</label>
                <form:select path="departamento" id="departamento" cssClass="form-select">
                    <form:option value="" label="Elige tu departamento" />
                    <form:option value="Santander" label="Santander" />
                    <form:option value="Cundinamarca" label="Cundinamarca" />
                    <form:option value="Antioquia" label="Antioquia" />
                    <form:option value="Valle del Cauca" label="Valle del Cauca" />
                    <form:option value="Atlantico" label="Atlantico" />
                    <form:option value="Bolivar" label="Bolivar" />
                    <form:option value="Norte de Santander" label="Norte de Santander" />
                    <form:option value="Otro" label="Otro" />
                </form:select>
            </div>
            <div class="col-md-6">
                <label class="form-label small" for="ciudad">Ciudad</label>
                <form:select path="ciudad" id="ciudad" cssClass="form-select">
                    <form:option value="" label="Elige tu ciudad" />
                    <form:option value="Bucaramanga" label="Bucaramanga" />
                    <form:option value="Floridablanca" label="Floridablanca" />
                    <form:option value="Giron" label="Giron" />
                    <form:option value="Piedecuesta" label="Piedecuesta" />
                    <form:option value="Bogota" label="Bogota" />
                    <form:option value="Medellin" label="Medellin" />
                    <form:option value="Cali" label="Cali" />
                    <form:option value="Barranquilla" label="Barranquilla" />
                    <form:option value="Cartagena" label="Cartagena" />
                    <form:option value="Cucuta" label="Cucuta" />
                    <form:option value="Otra" label="Otra" />
                </form:select>
            </div>
        </div>

        <div class="d-flex align-items-center gap-2 mb-2">
            
            <span class="small text-muted">Se guarda cifrado. El administrador no lo puede ver</span>
        </div>
        <div class="row g-3 mb-4">
            <div class="col-md-3">
                <label class="form-label small" for="tipoDocumento">Tipo de documento</label>
                <form:select path="tipoDocumento" id="tipoDocumento" cssClass="form-select">
                    <form:option value="CC" label="Cedula de ciudadania" />
                    <form:option value="CE" label="Cedula de extranjeria" />
                    <form:option value="TI" label="Tarjeta de identidad" />
                    <form:option value="PA" label="Pasaporte" />
                </form:select>
            </div>
            <div class="col-md-3">
                <label class="form-label small" for="numeroDocumento">Numero</label>
                <form:input path="numeroDocumento" id="numeroDocumento" cssClass="form-control" />
            </div>
            <div class="col-md-3">
                <label class="form-label small" for="telefono">Telefono</label>
                <form:input path="telefono" id="telefono" cssClass="form-control" />
            </div>
            <div class="col-md-3">
                <label class="form-label small" for="fechaNacimiento">Fecha de nacimiento</label>
                <form:input path="fechaNacimiento" id="fechaNacimiento" type="date" cssClass="form-control" />
            </div>
            <div class="col-12">
                <label class="form-label small" for="direccion">Direccion de entrega</label>
                <form:input path="direccion" id="direccion" cssClass="form-control" />
            </div>
        </div>

        <div class="cinta cinta-fina mb-4"></div>

        <div class="form-check mb-2">
            <form:checkbox path="aceptaTratamiento" id="aceptaTratamiento" cssClass="form-check-input" />
            <label class="form-check-label small" for="aceptaTratamiento">
                Autorizo el tratamiento de mis datos segun la
                <a href="${ctx}/politica-datos" target="_blank">politica de tratamiento</a>.
            </label>
            <div><form:errors path="aceptaTratamiento" cssClass="text-danger small" /></div>
        </div>
        <div class="form-check mb-4">
            <form:checkbox path="autorizaMarketing" id="autorizaMarketing" cssClass="form-check-input" />
            <label class="form-check-label small" for="autorizaMarketing">
                Quiero recibir novedades y descuentos. Puedo revocarlo cuando quiera.
            </label>
        </div>

        <button class="btn btn-tinta px-4 py-2">Crear cuenta</button>
        <a href="${ctx}/login" class="btn btn-link">Ya tengo cuenta</a>
    </form:form>
</section>

<script src="${ctx}/recursos/js/seguridad-clave.js"></script>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
