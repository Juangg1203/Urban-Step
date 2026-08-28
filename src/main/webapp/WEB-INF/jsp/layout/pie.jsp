<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<footer class="pie">
    <div class="container">
        <div class="row g-4">
            <div class="col-md-4">
                <p class="display fs-4 mb-2 navbar-brand p-0 d-flex align-items-center gap-2">
                    <img src="${ctx}/recursos/img/logo.jpg" alt="UrbanStep" class="marca-logo">
                    Urban<span>Step</span>
                </p>
                <p class="small mb-1">Ropa y calzado para moverse por la ciudad.</p>
                <p class="small mb-0">Calle 36 # 22-15, Bucaramanga</p>
                <p class="small">WhatsApp 300 000 0000</p>
            </div>
            <div class="col-md-4">
                <p class="rotulo">Tienda</p>
                <p class="small mb-1"><a href="${ctx}/catalogo">Catalogo completo</a></p>
                <p class="small mb-1"><a href="${ctx}/catalogo?linea=ROPA">Ropa</a></p>
                <p class="small mb-1"><a href="${ctx}/catalogo?linea=CALZADO">Calzado</a></p>
            </div>
            <div class="col-md-4">
                <p class="rotulo">Ayuda</p>
                <p class="small mb-1"><a href="${ctx}/mi-cuenta">Mi cuenta y mis pedidos</a></p>
                <p class="small mb-1"><a href="${ctx}/politica-datos">Politica de privacidad</a></p>
                <p class="small text-muted mb-0">El chat responde las 24 horas. Los asesores atienden
                    de lunes a sabado, 8:00 a 18:00.</p>
            </div>
        </div>
        <div class="cinta cinta-fina my-4"></div>
        <p class="small text-muted mb-0">UrbanStep SAS &middot;
            <a href="${ctx}/politica-datos">Politica de privacidad</a> &middot;
            Ley 1581 de 2012 &middot; Proyecto academico</p>
    </div>
</footer>

<!-- ================= Chatbot de atencion ================= -->
<button id="boton-chat" aria-label="Abrir el chat de atencion">
    <i class="bi bi-chat-dots-fill"></i>
</button>

<section id="panel-chat" aria-live="polite">
    <header class="d-flex justify-content-between align-items-center">
        <div>
            <strong class="display">Aguja</strong>
            <div class="dato text-muted">Atencion 24/7</div>
        </div>
        <button id="cerrar-chat" type="button" class="btn btn-sm" aria-label="Cerrar el chat">
            <i class="bi bi-x-lg"></i>
        </button>
    </header>

    <div id="chat-mensajes"></div>
    <div id="chat-sugerencias" class="px-3 pt-2"></div>

    <form id="chat-form" class="d-flex gap-2 p-3 chat-pie">
        <label for="chat-texto" class="visually-hidden">Escribe tu mensaje</label>
        <input id="chat-texto" class="form-control" autocomplete="off"
               placeholder="Escribe tu pregunta" maxlength="500">
        <button class="btn btn-tinta px-3" type="submit" aria-label="Enviar">
            <i class="bi bi-send"></i>
        </button>
    </form>

    <div id="chat-calificacion" class="d-none p-3">
        <p class="rotulo mb-1">Como estuvo la atencion?</p>
        <div id="estrellas" class="mb-2"></div>
        <textarea id="chat-recomendacion" class="form-control form-control-sm mb-2" rows="2"
                  placeholder="Que recomendarias para mejorar? (opcional)"></textarea>
        <button id="enviar-calificacion" class="btn btn-hilo btn-sm w-100">Enviar calificacion</button>
    </div>
</section>

<script>window.CTX = '${ctx}';</script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="${ctx}/recursos/js/chat.js"></script>
</body>
</html>
