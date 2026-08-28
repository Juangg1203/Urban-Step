<%@ include file="/WEB-INF/jsp/layout/cabecera.jsp" %>

<section class="container py-5" style="max-width:900px;">
    <a href="${ctx}/" class="enlace-volver">&larr; Volver al inicio</a>
    <p class="rotulo mb-1">Habeas data</p>
    <h1 class="mb-3">Politica de tratamiento de datos personales</h1>
    <p class="text-muted">Version v1.0-2026 &middot; Urban<span>Step</span> SAS &middot; Ley 1581 de 2012 y
        Decreto 1377 de 2013.</p>

    <div class="cinta cinta-fina my-4"></div>

    <h2 class="fs-4 mt-4">1. Que datos recogemos y como los clasificamos</h2>

    <div class="bloque-dato publico p-3 mb-3">
        <span class="nivel nivel-publico">Publico</span>
        <p class="mb-1 mt-2 fw-bold">Nombres, apellidos, ciudad, departamento y ocupacion.</p>
        <p class="small mb-0">No requieren autorizacion especial. Cualquier miembro del equipo puede
            consultarlos para atender tu caso, y toda consulta queda registrada.</p>
    </div>

    <div class="bloque-dato semiprivado p-3 mb-3">
        <span class="nivel nivel-semiprivado">Semiprivado</span>
        <p class="mb-1 mt-2 fw-bold">Historial de compras, medios de pago usados y estado de pago.</p>
        <p class="small mb-0">Interesan a la empresa y a ti. Los consultan atencion al cliente, marketing
            y administracion, siempre con registro de auditoria.</p>
    </div>

    <div class="bloque-dato privado p-3 mb-3">
        <span class="nivel nivel-privado">Privado</span>
        <p class="mb-1 mt-2 fw-bold">Documento de identidad, direccion, telefono, correo personal y fecha
            de nacimiento.</p>
        <p class="small mb-0">Se guardan cifrados con AES-256. Solo tu y el oficial de proteccion de datos
            los ven completos; el agente de atencion los ve enmascarados para verificar tu identidad
            (por ejemplo 10*****32) y el administrador del sistema no tiene acceso.</p>
    </div>

    <div class="bloque-dato sensible p-3 mb-4">
        <span class="nivel nivel-sensible">Sensible</span>
        <p class="mb-1 mt-2 fw-bold">Medidas corporales, alergias a materiales, condiciones de movilidad
            y restricciones de vestimenta por creencias.</p>
        <p class="small mb-0">Son opcionales. No estas obligado a darlos y no pierdes ningun servicio si
            no los das. Se guardan cifrados y ninguna persona de la empresa puede leerlos: la tienda solo
            usa estadisticas agregadas y anonimas, por ejemplo cuantas unidades cortar de cada talla.</p>
    </div>

    <h2 class="fs-4 mt-4">2. Para que los usamos</h2>
    <ul>
        <li>Procesar y entregar tus pedidos.</li>
        <li>Atender tus consultas por chat, correo o tienda.</li>
        <li>Producir la curva de tallas de cada coleccion, con datos agregados y sin identificarte.</li>
        <li>Enviarte comunicaciones comerciales, solo si lo autorizaste. Puedes revocarlo cuando quieras.</li>
    </ul>

    <h2 class="fs-4 mt-4">3. Tus derechos</h2>
    <p>Desde <a href="${ctx}/mi-cuenta">Mi cuenta</a> puedes conocer, actualizar y rectificar tus datos,
        revocar la autorizacion y pedir su eliminacion. Cuando revocas la autorizacion de datos sensibles,
        el sistema los borra en ese momento: no se conservan copias "por si acaso".</p>

    <h2 class="fs-4 mt-4">4. Seguridad</h2>
    <ul>
        <li>Los datos privados y sensibles se cifran antes de guardarse en la base de datos.</li>
        <li>El acceso depende del rol; nadie ve un nivel que no necesita para su trabajo.</li>
        <li>Cada consulta o cambio queda en un registro de auditoria con usuario, rol, fecha e IP.</li>
        <li>Las claves se guardan con BCrypt; ni el equipo tecnico puede leerlas.</li>
    </ul>

    <h2 class="fs-4 mt-4">5. Contacto del responsable</h2>
    <p class="mb-0">Urban<span>Step</span> SAS &middot; Calle 36 # 22-15, Bucaramanga &middot;
        datos@urbanstep.com &middot; Respuesta a peticiones en maximo 15 dias habiles.</p>
</section>

<%@ include file="/WEB-INF/jsp/layout/pie.jsp" %>
