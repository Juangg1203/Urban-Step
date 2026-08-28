<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>
<c:set var="r" value="${reporte}" />

<section class="container py-5">
    <a href="${ctx}/panel" class="enlace-volver">&larr; Volver al panel</a>

    <!-- ============ encabezado y controles ============ -->
    <div class="d-flex flex-wrap justify-content-between align-items-end gap-3 mb-3">
        <div>
            <p class="rotulo mb-1">Reporte mensual de atencion al cliente</p>
            <h1 class="mb-1 text-capitalize">${r.nombreMes} de ${r.anio}</h1>
            <p class="dato text-muted mb-0">Periodo ${r.periodo} &middot; generado el ${r.fechaGeneracion}
                por ${r.generadoPor}</p>
        </div>

        <form class="row g-2 align-items-end no-imprimir" method="get" action="${ctx}/panel/reportes">
            <div class="col-auto">
                <label class="rotulo" for="mes">Mes</label>
                <select id="mes" name="mes" class="form-select form-select-sm">
                    <c:forEach begin="1" end="12" var="m">
                        <option value="${m}" ${r.mes eq m ? 'selected' : ''}>${m}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-auto">
                <label class="rotulo" for="anio">Ano</label>
                <select id="anio" name="anio" class="form-select form-select-sm">
                    <c:forEach var="a" items="${anios}">
                        <option value="${a}" ${r.anio eq a ? 'selected' : ''}>${a}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-auto"><button class="btn btn-tinta btn-sm">Generar</button></div>
        </form>
    </div>

    <div class="d-flex flex-wrap gap-2 mb-4 no-imprimir">
        <form method="post" action="${ctx}/panel/reportes/guardar" class="d-inline">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
            <input type="hidden" name="anio" value="${r.anio}">
            <input type="hidden" name="mes" value="${r.mes}">
            <button class="btn btn-hilo btn-sm">Guardar en el historico</button>
        </form>
        <a class="btn btn-tinta btn-sm" href="${ctx}/panel/reportes/pdf?anio=${r.anio}&mes=${r.mes}">
            Descargar PDF
        </a>
        <a class="btn btn-contorno btn-sm" href="${ctx}/panel/reportes/csv?anio=${r.anio}&mes=${r.mes}">
            Descargar CSV
        </a>
        <button class="btn btn-contorno btn-sm" onclick="window.print()">Imprimir o guardar en PDF</button>
    </div>

    <div class="cinta"></div>

    <c:if test="${r.sinDatos}">
        <div class="bloque-dato p-4 mt-4">
            <p class="fw-bold mb-1">No hubo atenciones registradas en este periodo.</p>
            <p class="mb-0 small">Elige otro mes o revisa que los casos se esten cerrando en el sistema.</p>
        </div>
    </c:if>

    <c:if test="${not r.sinDatos}">

    <!-- ============ 1. personas atendidas ============ -->
    <h2 class="fs-4 mt-5 mb-3">1. Cuantas personas se atendieron</h2>
    <div class="row g-3">
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1">Personas atendidas</p>
                <p class="cifra acento mb-1">${r.personasAtendidas}</p>
                <c:choose>
                    <c:when test="${r.variacionPersonas > 0}">
                        <p class="dato mb-0" style="color:var(--verde);">+${r.variacionPersonas}% vs mes anterior</p>
                    </c:when>
                    <c:when test="${r.variacionPersonas < 0}">
                        <p class="dato mb-0" style="color:var(--carmin);">${r.variacionPersonas}% vs mes anterior</p>
                    </c:when>
                    <c:otherwise><p class="dato text-muted mb-0">sin variacion</p></c:otherwise>
                </c:choose>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1">Clientes registrados</p>
                <p class="cifra mb-0">${r.clientesRegistrados}</p>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1">Visitantes sin cuenta</p>
                <p class="cifra mb-0">${r.visitantesAnonimos}</p>
            </div>
        </div>
        <div class="col-6 col-lg-3">
            <div class="tablero">
                <p class="rotulo mb-1">Total de atenciones</p>
                <p class="cifra mb-0">${r.totalAtenciones}</p>
            </div>
        </div>
    </div>

    <div class="row g-4 mt-1">
        <div class="col-lg-7">
            <div class="ficha p-4 h-100">
                <p class="rotulo mb-3">Evolucion de los ultimos 6 meses</p>
                ${gEvolucion}
            </div>
        </div>
        <div class="col-lg-5">
            <div class="ficha p-4 h-100">
                <p class="rotulo mb-3">Reparto por canal</p>
                ${gCanal}
            </div>
        </div>
    </div>

    <div class="row g-4 mt-1">
        <div class="col-lg-6">
            <div class="ficha p-4 h-100">
                <p class="rotulo mb-3">Por canal de atencion</p>
                <c:forEach var="canal" items="${r.porCanal}">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>${canal.etiqueta}</span>
                        <span class="dato">${canal.cantidad} &middot; ${canal.porcentaje}%</span>
                    </div>
                    <div class="barra-medida mb-3"><span style="width:${canal.porcentaje}%"></span></div>
                </c:forEach>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="ficha p-4 h-100">
                <p class="rotulo mb-3">Por tema consultado</p>
                ${gTema}
                <div class="cinta cinta-fina my-3"></div>
                <c:forEach var="tema" items="${r.porTema}">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>${tema.etiqueta}</span>
                        <span class="dato">${tema.cantidad} &middot; ${tema.porcentaje}%</span>
                    </div>
                    <div class="barra-medida mb-3"><span style="width:${tema.porcentaje}%"></span></div>
                </c:forEach>
            </div>
        </div>
    </div>

    <!-- ============ 2. calificacion ============ -->
    <h2 class="fs-4 mt-5 mb-3">2. Como calificaron la atencion</h2>
    <div class="row g-4">
        <div class="col-lg-4">
            <div class="tablero h-100">
                <p class="rotulo mb-1">Calificacion promedio</p>
                <p class="cifra acento mb-1">${r.promedioCalificacion} <span class="fs-5 text-muted">/ 5</span></p>
                <p class="estrellas fs-4 mb-2">
                    <c:forEach begin="1" end="5" var="i">
                        <c:choose>
                            <c:when test="${i <= r.promedioCalificacion}">&#9733;</c:when>
                            <c:otherwise><span style="color:var(--linea)">&#9733;</span></c:otherwise>
                        </c:choose>
                    </c:forEach>
                </p>
                <p class="dato text-muted mb-0">Mes anterior: ${r.promedioMesAnterior} / 5</p>
                <p class="dato mb-0" style="color:${r.variacionCalificacion >= 0 ? 'var(--verde)' : 'var(--carmin)'};">
                    ${r.variacionCalificacion >= 0 ? '+' : ''}${r.variacionCalificacion} puntos
                </p>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="tablero h-100 text-center">
                <p class="rotulo mb-2">Satisfaccion y resolucion</p>
                <div class="d-flex justify-content-center gap-2">
                    <div style="max-width:130px;">${gAnillo}</div>
                    <div style="max-width:130px;">${gResueltas}</div>
                </div>
                <p class="small text-muted mt-2 mb-0">4 y 5 estrellas sobre
                    ${r.atencionesCalificadas} respuestas. ${r.escaladas} casos escalados.</p>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="ficha p-4 h-100">
                <p class="rotulo mb-3">Distribucion de estrellas</p>
                ${gEstrellas}
                <div class="cinta cinta-fina my-3"></div>
                <%-- la lista viene ordenada de 5 a 1 estrellas --%>
                <c:forEach var="e" items="${r.distribucionEstrellas}" varStatus="fila">
                    <div class="d-flex justify-content-between small mb-1">
                        <span>${e.etiqueta}</span>
                        <span class="dato">${e.cantidad} &middot; ${e.porcentaje}%</span>
                    </div>
                    <div class="barra-medida mb-2 ${fila.index <= 1 ? 'buena' : (fila.index eq 2 ? '' : 'mala')}">
                        <span style="width:${e.porcentaje}%"></span>
                    </div>
                </c:forEach>
            </div>
        </div>
    </div>

    <!-- ============ 3. recomendaciones ============ -->
    <h2 class="fs-4 mt-5 mb-1">3. Que recomendaron los clientes</h2>
    <p class="text-muted small mb-3">Comentarios textuales, ordenados de la peor a la mejor calificacion.
        Los primeros son los que exigen accion.</p>

    <c:if test="${empty r.recomendaciones}">
        <div class="bloque-dato p-4">
            <p class="mb-0">Nadie dejo comentarios escritos este mes. Considere pedirlos al cerrar cada caso.</p>
        </div>
    </c:if>

    <div class="row g-3">
        <c:forEach var="rec" items="${r.recomendaciones}">
            <div class="col-lg-6">
                <div class="ficha p-3 h-100" style="border-left:4px solid ${rec.negativa ? 'var(--carmin)' : 'var(--verde)'};">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <span class="fw-bold small">${rec.cliente}</span>
                            <span class="dato text-muted"> &middot; ${rec.fecha} &middot; ${rec.canal}</span>
                        </div>
                        <span class="estrellas"><c:forEach begin="1" end="${rec.estrellas}">&#9733;</c:forEach></span>
                    </div>
                    <p class="mb-2 small">${rec.texto}</p>
                    <span class="rotulo">${rec.tema}</span>
                </div>
            </div>
        </c:forEach>
    </div>

    <!-- ============ 4. sugerencias ============ -->
    <h2 class="fs-4 mt-5 mb-1">4. Sugerencias para la administracion</h2>
    <p class="text-muted small mb-3">Derivadas de los indicadores anteriores. Cada una indica el dato que
        la origina para que se pueda verificar.</p>

    <c:forEach var="s" items="${r.sugerencias}">
        <div class="sugerencia ${s.prioridad eq 'ALTA' ? 'alta' : (s.prioridad eq 'MEDIA' ? 'media' : 'baja')} p-3 mb-3">
            <div class="d-flex justify-content-between align-items-start mb-1">
                <h3 class="fs-6 mb-0">${s.titulo}</h3>
                <span class="nivel ${s.prioridad eq 'ALTA' ? 'nivel-sensible' :
                    (s.prioridad eq 'MEDIA' ? 'nivel-privado' : 'nivel-publico')}">
                    Prioridad ${s.prioridad}
                </span>
            </div>
            <p class="small mb-2">${s.detalle}</p>
            <p class="dato text-muted mb-0">Indicador: ${s.indicador}</p>
        </div>
    </c:forEach>

    </c:if>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
