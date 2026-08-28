/* =====================================================================
   Widget del chatbot de atencion al cliente.
   Habla con /api/chat: iniciar, mensaje, escalar y calificar.
   ===================================================================== */
(function () {
    const boton      = document.getElementById('boton-chat');
    const panel      = document.getElementById('panel-chat');
    const cerrar     = document.getElementById('cerrar-chat');
    const lista      = document.getElementById('chat-mensajes');
    const chips      = document.getElementById('chat-sugerencias');
    const formulario = document.getElementById('chat-form');
    const entrada    = document.getElementById('chat-texto');
    const cajaCalif  = document.getElementById('chat-calificacion');
    const estrellas  = document.getElementById('estrellas');
    const comentario = document.getElementById('chat-recomendacion');
    const enviarCal  = document.getElementById('enviar-calificacion');

    if (!boton || !panel) return;

    let sesion = null;
    let iniciado = false;
    let calificacion = 0;

    // ---------- utilidades de pantalla ----------
    function burbuja(texto, tipo) {
        const div = document.createElement('div');
        div.className = 'burbuja ' + tipo;
        div.textContent = texto;
        lista.appendChild(div);
        lista.scrollTop = lista.scrollHeight;
        return div;
    }

    function pintarSugerencias(opciones) {
        chips.innerHTML = '';
        (opciones || []).forEach(function (texto) {
            const chip = document.createElement('button');
            chip.type = 'button';
            chip.className = 'chip';
            chip.textContent = texto;
            chip.addEventListener('click', function () {
                chips.innerHTML = '';
                enviar(texto);
            });
            chips.appendChild(chip);
        });
    }

    function pedir(ruta, cuerpo) {
        return fetch(window.CTX + '/api/chat/' + ruta, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(cuerpo || {})
        }).then(function (r) {
            if (!r.ok) throw new Error('respuesta ' + r.status);
            return r.json();
        });
    }

    // ---------- ciclo de la conversacion ----------
    function iniciar() {
        if (iniciado) return;
        iniciado = true;
        pedir('iniciar', {})
            .then(function (r) {
                sesion = r.sesion;
                burbuja(r.respuesta, 'bot');
                pintarSugerencias(r.sugerencias);
            })
            .catch(function () {
                burbuja('El chat no esta disponible en este momento. Escribenos a '
                      + 'hola@urbanstep.com y te respondemos hoy mismo.', 'aviso');
            });
    }

    function enviar(texto) {
        burbuja(texto, 'cliente');
        const cargando = burbuja('Escribiendo...', 'bot');

        pedir('mensaje', { sesion: sesion, texto: texto })
            .then(function (r) {
                cargando.remove();
                sesion = r.sesion || sesion;
                burbuja(r.respuesta, 'bot');
                pintarSugerencias(r.sugerencias);
                if (r.escalar) {
                    burbuja('Tu caso quedo en la fila de un asesor humano. Te responden por correo '
                          + 'en horario de atencion.', 'aviso');
                }
                if (r.pedirCalificacion) mostrarCalificacion();
            })
            .catch(function () {
                cargando.remove();
                burbuja('No pude enviar el mensaje. Revisa tu conexion e intenta de nuevo.', 'aviso');
            });
    }

    function mostrarCalificacion() {
        cajaCalif.classList.remove('d-none');
        cajaCalif.scrollIntoView({ block: 'nearest' });
    }

    // ---------- estrellas ----------
    for (let i = 1; i <= 5; i++) {
        const b = document.createElement('button');
        b.type = 'button';
        b.className = 'estrella-btn';
        b.textContent = '\u2605';
        b.setAttribute('aria-label', i + ' de 5');
        b.addEventListener('click', function () {
            calificacion = i;
            Array.from(estrellas.children).forEach(function (hijo, indice) {
                hijo.classList.toggle('activa', indice < i);
            });
        });
        estrellas.appendChild(b);
    }

    enviarCal.addEventListener('click', function () {
        if (calificacion === 0) {
            burbuja('Elige de 1 a 5 estrellas para poder registrar tu calificacion.', 'aviso');
            return;
        }
        pedir('calificar', {
            sesion: sesion,
            estrellas: String(calificacion),
            recomendacion: comentario.value
        }).then(function (r) {
            cajaCalif.classList.add('d-none');
            burbuja(r.mensaje, 'aviso');
        }).catch(function () {
            burbuja('No pudimos registrar la calificacion. Intenta de nuevo.', 'aviso');
        });
    });

    // ---------- apertura y cierre ----------
    function abrirChat() {
        panel.classList.add('abierto');
        boton.classList.add('abierto');
        boton.setAttribute('aria-label', 'Cerrar el chat de atencion');
        iniciar();
        entrada.focus();
    }

    function cerrarChat() {
        panel.classList.remove('abierto');
        boton.classList.remove('abierto');
        boton.setAttribute('aria-label', 'Abrir el chat de atencion');
        // Se pide la calificacion al cerrar: es el unico momento en que el
        // cliente todavia recuerda como fue la atencion.
        if (sesion && calificacion === 0) mostrarCalificacion();
    }

    boton.addEventListener('click', function () {
        if (panel.classList.contains('abierto')) cerrarChat();
        else abrirChat();
    });

    cerrar.addEventListener('click', cerrarChat);

    // Escape cierra el chat, como cualquier panel flotante.
    document.addEventListener('keydown', function (evento) {
        if (evento.key === 'Escape' && panel.classList.contains('abierto')) {
            cerrarChat();
            boton.focus();
        }
    });

    formulario.addEventListener('submit', function (evento) {
        evento.preventDefault();
        const texto = entrada.value.trim();
        if (!texto) return;
        entrada.value = '';
        chips.innerHTML = '';
        enviar(texto);
    });
})();
