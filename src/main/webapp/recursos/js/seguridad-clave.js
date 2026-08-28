/*
 * Medidor de seguridad de la clave, en vivo.
 * Replica las reglas de SeguridadClaveService para dar aviso mientras se
 * escribe. Es comodidad para quien se registra, NO seguridad: la decision
 * real la toma el servidor, porque este archivo se puede saltar.
 */
(function () {
  var PROHIBIDAS = ['123456', '1234567', '12345678', '123456789', '1234567890',
    'password', 'contrasena', 'clave', 'qwerty', 'abc123', '111111', 'colombia',
    'admin', 'administrador', 'usuario', 'iloveyou', 'bucaramanga', 'santander',
    'tiendaropa', 'urbanstep'];
  var SECUENCIAS = ['abcdef', 'qwerty', 'asdfgh', 'zxcvbn', '123456', '098765'];

  function evaluar(clave, usuario, correo) {
    var avisos = [];
    if (!clave) {
      return { puntaje: 0, etiqueta: 'Sin clave', nivel: 'vacia', avisos: [], aceptable: false };
    }
    var min = clave.toLowerCase();
    var p = 0;

    if (clave.length >= 12) p += 40;
    else if (clave.length >= 10) p += 30;
    else if (clave.length >= 8) p += 20;
    else { p += 5; avisos.push('Usa al menos 8 caracteres; 12 es mucho mejor'); }

    if (/[a-z]/.test(clave)) p += 10;
    if (/[A-Z]/.test(clave)) p += 15; else avisos.push('Agrega alguna mayuscula');
    if (/[0-9]/.test(clave)) p += 15; else avisos.push('Agrega algun numero');
    if (/[^A-Za-z0-9]/.test(clave)) p += 15;

    for (var i = 0; i < PROHIBIDAS.length; i++) {
      if (min.indexOf(PROHIBIDAS[i]) !== -1) {
        p -= 35; avisos.push('Contiene una palabra muy comun en claves filtradas'); break;
      }
    }
    for (var j = 0; j < SECUENCIAS.length; j++) {
      if (min.indexOf(SECUENCIAS[j]) !== -1) {
        p -= 20; avisos.push('Evita secuencias seguidas del teclado o de numeros'); break;
      }
    }
    if (/(.)\1{2,}/.test(clave)) {
      p -= 10; avisos.push('Evita repetir el mismo caracter tres veces seguidas');
    }
    if (usuario && usuario.length >= 4 && min.indexOf(usuario.toLowerCase()) !== -1) {
      p -= 30; avisos.push('No uses tu nombre de usuario dentro de la clave');
    }
    if (correo && correo.indexOf('@') > 0) {
      var parte = correo.substring(0, correo.indexOf('@')).toLowerCase();
      if (parte.length >= 4 && min.indexOf(parte) !== -1) {
        p -= 30; avisos.push('No uses tu correo dentro de la clave');
      }
    }
    if (/^[a-z]+$/.test(min) || /^[0-9]+$/.test(min)) {
      p -= 10; avisos.push('Mezcla letras, numeros y algun simbolo');
    }

    p = Math.max(0, Math.min(100, p));
    var etiqueta, nivel;
    if (p < 25)      { etiqueta = 'Muy debil';  nivel = 'muy-debil'; }
    else if (p < 50) { etiqueta = 'Debil';      nivel = 'debil'; }
    else if (p < 70) { etiqueta = 'Aceptable';  nivel = 'aceptable'; }
    else if (p < 90) { etiqueta = 'Buena';      nivel = 'buena'; }
    else             { etiqueta = 'Excelente';  nivel = 'excelente'; }

    return { puntaje: p, etiqueta: etiqueta, nivel: nivel, avisos: avisos, aceptable: p >= 50 };
  }


  /*
   * Generador de claves seguras.
   * Usa crypto.getRandomValues, no Math.random: este ultimo es predecible y
   * no sirve para nada que tenga que ver con seguridad.
   */
  function generarClave(largo) {
    largo = largo || 14;
    var MINUS = 'abcdefghijkmnopqrstuvwxyz';   // sin la l, se confunde con 1
    var MAYUS = 'ABCDEFGHJKLMNPQRSTUVWXYZ';    // sin I ni O
    var NUMS  = '23456789';                    // sin 0 ni 1
    var SIMB  = '!@#$%&*?-+=';
    var TODOS = MINUS + MAYUS + NUMS + SIMB;

    function azar(max) {
      var buffer = new Uint32Array(1);
      window.crypto.getRandomValues(buffer);
      return buffer[0] % max;
    }
    function tomar(alfabeto) {
      return alfabeto.charAt(azar(alfabeto.length));
    }

    // Se garantiza al menos uno de cada tipo, y el resto al azar.
    var caracteres = [tomar(MINUS), tomar(MAYUS), tomar(NUMS), tomar(SIMB)];
    for (var i = caracteres.length; i < largo; i++) {
      caracteres.push(tomar(TODOS));
    }
    // Barajado Fisher-Yates para que los obligatorios no queden siempre al inicio.
    for (var j = caracteres.length - 1; j > 0; j--) {
      var k = azar(j + 1);
      var tmp = caracteres[j];
      caracteres[j] = caracteres[k];
      caracteres[k] = tmp;
    }
    return caracteres.join('');
  }

  document.addEventListener('DOMContentLoaded', function () {
    var campo = document.getElementById('clave');
    var barra = document.getElementById('barraClave');
    var texto = document.getElementById('textoClave');
    var lista = document.getElementById('avisosClave');
    if (!campo || !barra) return;

    var usuario = document.getElementById('nombreUsuario');
    var correo = document.getElementById('correo');
    var ojo = document.getElementById('verClave');
    var sugerir = document.getElementById('sugerirClave');
    var cajaSugerida = document.getElementById('claveSugerida');

    function pintar() {
      var r = evaluar(campo.value,
                      usuario ? usuario.value : '',
                      correo ? correo.value : '');
      barra.style.width = r.puntaje + '%';
      barra.className = 'medidor-relleno ' + r.nivel;
      texto.textContent = campo.value ? r.etiqueta + ' (' + r.puntaje + '/100)' : '';
      texto.className = 'dato medidor-texto ' + r.nivel;

      lista.innerHTML = '';
      r.avisos.forEach(function (a) {
        var li = document.createElement('li');
        li.textContent = a;
        lista.appendChild(li);
      });
      if (campo.value && r.aceptable && r.avisos.length === 0) {
        var ok = document.createElement('li');
        ok.className = 'ok';
        ok.textContent = 'Esta clave es solida.';
        lista.appendChild(ok);
      }
    }

    campo.addEventListener('input', pintar);
    if (usuario) usuario.addEventListener('input', pintar);
    if (correo) correo.addEventListener('input', pintar);

    if (ojo) {
      ojo.addEventListener('click', function () {
        var oculto = campo.type === 'password';
        campo.type = oculto ? 'text' : 'password';
        ojo.textContent = oculto ? 'Ocultar' : 'Ver';
      });
    }

    if (sugerir) {
      sugerir.addEventListener('click', function () {
        var propuesta = generarClave(14);
        campo.value = propuesta;
        // Se muestra en claro: una clave que no puedes leer no la puedes guardar.
        campo.type = 'text';
        if (ojo) ojo.textContent = 'Ocultar';
        cajaSugerida.innerHTML =
          '<span class="clave-sugerida-texto">' + propuesta + '</span>' +
          '<span class="clave-sugerida-nota">Copiala en tu gestor de claves antes de continuar: ' +
          'no la vas a volver a ver.</span>';
        cajaSugerida.hidden = false;
        pintar();
      });
    }
  });
})();
