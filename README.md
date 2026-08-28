# UrbanStep — sitio web de ropa y calzado

Proyecto en **Spring Boot 3 + JSP + Bootstrap 5 + MySQL**, sin servlets escritos a mano.
Cubre los cuatro requisitos del enunciado:

1. Recolección de datos de clientes clasificados en **públicos, semiprivados, privados y sensibles**,
   con reglas de acceso distintas por nivel.
2. **Chatbot** de atención al cliente disponible 24/7.
3. **Reporte mensual** con personas atendidas, calificación de la atención y recomendaciones escritas.
4. **Sugerencias automáticas para la administración**, derivadas de los indicadores del mes.

---

## 1. Requisitos

| Herramienta | Versión |
|---|---|
| JDK | 21 |
| Maven | 3.9+ (o el wrapper de tu IDE) |
| MySQL | 8.x (sirve el de XAMPP) |
| Navegador | cualquiera |

---

## 2. Poner a andar el proyecto

**Paso 1 — crear la base de datos.**
Abre phpMyAdmin (XAMPP) o MySQL Workbench y ejecuta, en este orden:

```
basedatos/01_esquema.sql
basedatos/02_datos_iniciales.sql
```

**Paso 2 — configurar la conexión.**
En `src/main/resources/application.properties` ajusta usuario y clave:

```properties
spring.datasource.username=root
spring.datasource.password=
```

Si en tu XAMPP usas el usuario `estudiantes`, cambia esas dos líneas por ese usuario y su clave.

**Paso 3 — ejecutar.**

```bash
mvn clean spring-boot:run
```

O desde Eclipse / IntelliJ: clic derecho sobre `TiendaRopaApplication.java` → Run as → Spring Boot App.

**Paso 4 — abrir** `http://localhost:8080`

> El empaquetado es **war**, no jar. Es la única forma de que las JSP funcionen con el Tomcat
> embebido de Spring Boot. `mvn clean package` genera `target/tienda-ropa.war`, que sirve tanto
> para `java -jar` como para un Tomcat externo.

### Si ya tenías la base creada antes

Si al entrar al panel aparece `No enum constant com.tiendaropa.model.Rol.AGENTE`, la base todavía
tiene filas con los roles viejos. Dos salidas:

- **Empezar limpio** (recomendado): `DROP DATABASE tienda_ropa;` y volver a ejecutar `01_esquema.sql`.
- **Conservar los datos**: ejecutar `basedatos/03_migracion_roles.sql`, que convierte los roles
  antiguos a los nuevos.

---

## 2.b Idiomas

El sitio está en español por defecto y se puede cambiar a inglés con el selector **ES / EN** de la
barra superior, o agregando `?lang=en` a cualquier URL. El idioma se guarda en la sesión.

Hay **136 claves** traducidas, aplicadas en la navegación, el inicio, el catálogo, la ficha de
producto, el carrito, el checkout, el login y la pantalla de pedidos del cliente.

Lo que **sigue en español** aunque cambies a inglés, y conviene saberlo antes de sustentar:

- Las pantallas del panel interno (aprobaciones, auditoría, reporte mensual). Se usan solo dentro de
  la empresa, así que se priorizó lo que ve el cliente.
- Los nombres de los estados (`Esperando visto bueno del jefe`, `Despachado`...) y las respuestas del
  chatbot, porque vienen de enums y de la base de conocimiento en Java, no de las JSP.
- Los datos que escribe el usuario: nombres de producto, descripciones, comentarios.

Para traducir más, se agrega la clave a `messages_es.properties` **y** a `messages_en.properties`, y
se usa en la JSP con `<spring:message code="mi.clave" />`. El selector conserva la página pero no los
filtros de la URL: `?lang=xx` reemplaza la cadena de consulta.

---

## 3. Usuarios de prueba

Se crean solos la primera vez (`app.datos-demo=true`), junto con el catálogo y **tres meses de
atenciones simuladas** para que el reporte tenga datos de inmediato.

| Usuario | Clave | Rol | Qué ve y qué hace |
|---|---|---|---|
| `admin` | `Admin123` | Administrador | Gestiona las cuentas del personal y ve el panel de supervisión. **No** administra productos ni ve datos privados o sensibles |
| `jefe` | `Jefe123` | Jefe | Gestiona el catálogo, da el visto bueno final a las compras (después del pago), audita accesos, ve datos privados completos y solo el *metadato* de los sensibles |
| `vendedor` | `Vendedor123` | Empleado (Vendedor) | Verifica que el pago entró. Datos privados **enmascarados** |
| `bodeguero` | `Bodeguero123` | Empleado (Bodeguero) | Alista y despacha. Datos privados **enmascarados** |
| `laura` | `Cliente123` | Cliente | Todos sus propios datos, sus pedidos, cotizaciones y reseñas |

Otros clientes de prueba con la misma clave: `carlos`, `sofia`, `julian`, `marcela`, `andres`.

Para desactivar los datos de prueba: `app.datos-demo=false`.

---

## 4. Los cuatro niveles de datos

| Nivel | Dónde vive | Qué guarda | Quién lo ve |
|---|---|---|---|
| Público | `cliente` | Nombres, ciudad, ocupación | Todos los roles internos y el titular |
| Semiprivado | `compra_cliente`, `pedido` | Historial de compras, pedidos y pagos | Empleado, Jefe, Admin |
| Privado | `dato_privado_cliente`, dirección de entrega en `pedido` (**cifrado**) | Documento, dirección, teléfono, correo, nacimiento | Titular y Jefe completo; Empleado enmascarado; **Admin no** |
| Sensible | `dato_sensible_cliente` (**cifrado**) | Medidas corporales, alergias a materiales, movilidad, restricciones de vestimenta | **Solo el titular** |

Decisiones detrás del diseño:

- **El administrador queda fuera de los niveles privado y sensible a propósito.** Administrar el
  sistema no exige leer la cédula ni las medidas de un cliente. Menos gente con acceso, menos
  superficie de fuga.
- **El jefe hereda la función de oficial de protección de datos**, porque es quien responde por el
  tratamiento: ve los privados completos y audita los accesos. Aun así, del nivel sensible solo ve el
  metadato — si existen y desde cuándo —, nunca el contenido.
- **El empleado se especializa en vendedor o bodeguero.** El subtipo cambia lo que puede *hacer*
  (verificar pagos o despachar), no lo que puede *ver*: el acceso a datos personales es idéntico.
- Los datos sensibles exigen **autorización expresa y revocable**. Revocar equivale a borrar: el
  sistema los elimina en ese momento.
- Cifrado **AES-256-GCM** con IV aleatorio por registro (`util/CifradoAes.java`), aplicado de forma
  transparente vía `@Convert(converter = ConvertidorCifrado.class)`.
- Toda consulta o cambio sobre datos personales — incluidos los **intentos denegados** — queda en
  `log_auditoria` con usuario, rol, nivel, fecha e IP. Se consulta en *Panel → Auditoría*.

La matriz de acceso está centralizada en **un solo archivo**: `service/PoliticaAccesoService.java`.
Si mañana cambia la política, se cambia ahí y no en veinte sitios.

---

## 5. Chatbot

- Widget flotante en todas las páginas. Responde **24/7**.
- Capa 1: motor de reglas (`service/BaseConocimiento.java`) con **24 intenciones** — saludo, qué sabe
  hacer, tallas, estar entre dos tallas, cuidado de la prenda, catálogo, recomendaciones por ocasión,
  promociones, envíos, estado del pedido, cancelaciones, pagos, seguridad del pago, cambios, registro,
  claves, datos personales, datos sensibles, eliminación de cuenta, horarios, quejas, escalamiento y
  despedida. Funciona **sin internet y sin llaves de API**.
- Cada respuesta ofrece **botones de seguimiento propios**, distintos según lo que se preguntó, para
  que la conversación siga en vez de terminar en cada turno. Una queja abierta escala sola a un agente:
  eso no lo resuelve un bot.
- Capa 2 (opcional): un modelo de lenguaje para lo que no cubran las reglas. Hay dos
  proveedores, ambos gratuitos:

  **A. Google Gemini** — llave gratis en `aistudio.google.com`, sin tarjeta. Necesita internet.

  ```properties
  app.chatbot.ia-habilitada=true
  app.chatbot.proveedor=gemini
  app.chatbot.gemini-api-key=TU_LLAVE
  ```

  **B. Ollama** — el modelo corre en tu propio PC. Sin llave y sin internet, así que no
  dependes del wifi el día de la sustentación. Instala Ollama, ejecuta `ollama pull llama3.2:3b`
  y déjalo corriendo:

  ```properties
  app.chatbot.ia-habilitada=true
  app.chatbot.proveedor=ollama
  ```

  Pide unos 4–8 GB de RAM libres y responde más lento que la nube (por eso el timeout es de
  60 segundos). Si el servicio no está arriba o falla, el chat sigue funcionando con reglas.

  Vale la pena decirlo: **para sustentar, el motor de reglas suele ser mejor opción.** Con reglas
  controlas exactamente qué responde el bot sobre envíos, cambios y tratamiento de datos. Un modelo
  generativo puede inventarse una política de devoluciones que no existe, justo en el tema que
  estás evaluando. Por eso la IA viene desactivada por defecto y siempre va después de las reglas.
- Capa 3: escalamiento a un agente humano (escribe "asesor").
- Cada conversación genera una **atención**, y al cerrarla el cliente deja **estrellas + comentario**.
  Eso es exactamente lo que alimenta el reporte del mes.

El chatbot nunca pide documento, dirección ni datos de tarjeta.

---

## 5.a Gestión de productos (CRUD)

*Panel → Productos*, restringido al **Jefe**. Es una decisión deliberada: administrar el sistema
(usuarios, seguridad, disponibilidad) no es lo mismo que vender, y el catálogo es parte del negocio
comercial, no de la administración técnica. El administrador no toca productos; ve el inventario en
su panel solo para supervisión.

Permite crear, consultar, editar, retirar y eliminar. Cada producto maneja: identificador, referencia
(SKU), nombre, descripción, categoría, precio, imagen, tallas, color, material, cantidad disponible,
nivel mínimo de existencias y estado.

**Todo el catálogo sale de la base de datos.** No hay ni un producto escrito en el HTML: lo que se
crea en el panel aparece de inmediato en la tienda.

Tres decisiones que vale la pena poder defender:

- **"Retirar" y "Eliminar" no son lo mismo.** Retirar lo saca del catálogo pero lo conserva, para que
  los pedidos que ya lo incluyen sigan cuadrando. Eliminar lo borra de verdad, y **solo se permite
  mientras el producto nunca se haya vendido** — si tiene ventas, el sistema lo impide y sugiere
  retirarlo. Borrar un producto vendido dejaría el historial apuntando a la nada.
- **Las imágenes se guardan en disco, no en la base.** En MySQL queda solo el nombre del archivo.
  Meter binarios en la base la infla, encarece los respaldos y obliga a pasar cada imagen por la
  aplicación. La carpeta se configura con `app.imagenes.carpeta` y va **fuera del proyecto**: si
  estuviera dentro del war, cada despliegue borraría lo que subió el jefe.
- **El nombre del archivo lo genera el servidor**, nunca se usa el que manda el navegador. Un archivo
  llamado `../../algo.jsp` podría escribir fuera de la carpeta prevista. También se valida tipo,
  extensión y tamaño (máximo 8 MB, pensado para fotos de celular).

Se puede subir un archivo o pegar una URL externa; si se hacen las dos cosas, gana el archivo.

## 5.b Carrito, pago directo y despacho

El carrito vive en la sesión del navegador, no en la base de datos: mientras el cliente no confirme
nada, no hay razón para guardar lo que está mirando. Se puede armar **sin cuenta**; el login se pide
al confirmar.

**El cliente paga directo, sin esperar una aprobación previa.** Lo único que el sistema controla
antes del pago es que no compre más de lo que hay en inventario — eso se valida dos veces: al pintar
el checkout y otra vez justo antes de generar el pedido, porque entre una cosa y otra pudo entrar
otra compra por las mismas unidades.

Desde el checkout salen dos caminos:

- **Guardar cotización** — el pedido queda en estado `COTIZACION` con los precios congelados. No
  compromete a nada; se puede continuar y pagar después, desde *Mis pedidos*.
- **Continuar y pagar** — el pedido pasa a `PENDIENTE_PAGO`. El inventario se descuenta en este
  momento, no cuando se confirme el pago.

Después del pago hay **dos filtros humanos en cascada**, cada uno responsable de una sola cosa y sin
ver el trabajo del otro:

| Estado | Lo mueve | Qué pasa |
|---|---|---|
| `COTIZACION` | Cliente | Precios congelados, sin compromiso |
| `PENDIENTE_PAGO` | Cliente | Paga con Wompi o reporta un pago manual, con comprobante opcional |
| `PAGO_EN_VERIFICACION` | **Vendedor** | Confirma que el dinero realmente entró — nada más |
| `PENDIENTE_ACEPTACION_JEFE` | **Jefe** | Visto bueno final. Si rechaza, exige motivo y el cliente lo lee |
| `PAGADO` → `EN_PREPARACION` → `DESPACHADO` | **Bodeguero** | Alista, despacha y registra la guía |
| `ENTREGADO` | **Cliente** | Confirma que lo recibió (con foto opcional) y ya puede dejar reseña |

Tres decisiones que vale la pena poder defender:

- **Un pedido nunca cambia de estado solo.** Cada paso lo ejecuta una persona con el rol adecuado,
  queda con nombre y fecha en el pedido, y se registra en la auditoría.
- **Cada rol ve solo su bandeja, no solo por seguridad de rutas.** La propia consulta a la base trae
  datos distintos según quién pregunta: el vendedor nunca ve la cola de despachos, el bodeguero nunca
  ve la de pagos. La única vista completa es la del administrador, en modo consulta.
- **La entrega la confirma el cliente, no el sistema.** Es lo que dispara el estado `ENTREGADO` y
  habilita la reseña del producto. El personal tiene un botón de respaldo por si el cliente nunca
  confirma, pero ese cierre manual no genera reseña — la reseña es de quien recibió, no de bodega.

El cliente sigue su pedido desde *Mis pedidos*, con una línea de tiempo que marca en qué paso va.

## 5.c Pago en línea con Wompi

La tienda se integra con **Wompi** (pasarela colombiana). El flujo tiene cuatro pasos:

1. La tienda arma un formulario con el monto, una referencia única y una **firma de integridad**, y
   envía al cliente al Checkout de Wompi.
2. El cliente paga allá. **Los datos de la tarjeta nunca pasan por este servidor.**
3. Wompi devuelve al cliente a `/pagos/wompi/retorno` con el id de la transacción, y además envía un
   evento al webhook `/api/pagos/wompi/eventos`.
4. La tienda **consulta el estado real** contra la API de Wompi antes de dar el pago por bueno.

### Cuatro decisiones de seguridad que conviene poder defender

- **Nunca se confía en la URL de retorno.** Cualquiera puede escribir a mano
  `...retorno?id=123&estado=APPROVED`. El estado se le pregunta a Wompi o se valida con la firma del
  evento; la URL solo sirve para saber *qué* transacción consultar.
- **La firma de integridad se calcula en el servidor.** Es un SHA-256 de referencia + monto + moneda
  + secreto. Sin ella, alguien podría copiar el formulario, cambiar el total a mil pesos y pagar. El
  secreto no puede estar en el navegador, o dejaría de ser secreto.
- **Se verifica el monto cobrado.** Si lo que reporta la pasarela no coincide con el total del
  pedido, el pago no se aprueba: pasa a verificación humana y queda en la auditoría.
- **La operación es idempotente.** El webhook y el retorno del cliente casi siempre llegan los dos, y
  a veces repetidos. Aplicar el mismo pago dos veces adelantaría el pedido sin razón.

### Dos modos: simulado y real

```properties
app.pago.wompi.habilitado=true
app.pago.wompi.modo=simulado    # o "real"
```

**Modo simulado (el que viene activo).** Una pasarela local reproduce el flujo completo: referencia
única, firma del monto, validación de esa firma, resultado aplicado de forma idempotente. Quien
prueba elige si el pago se aprueba, lo rechaza el banco o queda pendiente.

No es una maqueta. Recorre los mismos pasos y termina llamando al mismo método del servicio; lo único
que cambia es quién responde. Sirve para dos cosas: desarrollar sin depender de un trámite, y
**demostrar los casos malos** — provocar un rechazo a voluntad es difícil con una pasarela real.

La firma se valida también en modo simulado. Si no lo hiciera, el ejercicio no probaría nada: el
sentido de la firma es que el monto no se pueda alterar en el camino, y eso hay que poder mostrarlo.

**Modo real.** Requiere una cuenta de comercio en Wompi. Ten en cuenta que el ambiente de producción
exige RUT y documentos de la empresa; los requisitos del sandbox conviene verificarlos directamente
en su sitio, porque cambian.

### Cómo activar el modo real

Crea un comercio en `comercios.wompi.co` y copia las llaves del ambiente **sandbox**:

```properties
app.pago.wompi.habilitado=true
app.pago.wompi.modo=real
app.pago.wompi.llave-publica=pub_test_...
app.pago.wompi.llave-integridad=...
app.pago.wompi.llave-eventos=...
```

En sandbox no se mueve dinero real; se usan las tarjetas de prueba de la documentación. El webhook
necesita una URL pública, así que para probarlo en local hace falta un túnel (ngrok o similar) —
el retorno del cliente sí funciona sin eso.

Si la pasarela está deshabilitada, el botón no aparece y el cliente reporta su pago manualmente,
como antes. La integración es un añadido, no un reemplazo.

> Las llaves privadas y los secretos van como **variables de entorno**, nunca dentro del repositorio.

## 5.d Gestión de usuarios (solo Administrador)

*Panel → Gestionar usuarios*. Es el trabajo real del administrador según el enunciado: mantener el
sitio funcional y seguro, lo que incluye quién tiene acceso y con qué rol — no vender ni tocar el
catálogo, eso quedó en manos del jefe.

Permite crear, editar, restablecer la clave y activar o desactivar cuentas de **personal interno**
(empleado, jefe, administrador). Los clientes no aparecen aquí: se registran solos en `/registro` y
no tiene sentido que el administrador les asigne un rol.

Dos decisiones:

- **No hay borrado definitivo de usuarios.** Desactivar bloquea el inicio de sesión pero conserva la
  cuenta, para que los pedidos y decisiones que quedaron a su nombre sigan teniendo sentido en el
  historial y la auditoría.
- **La clave nueva pasa por el mismo validador que el registro público** (`SeguridadClaveService`):
  el administrador no puede crear una cuenta con una clave débil solo porque tiene acceso al panel.

## 5.e Reseñas

Al confirmar la recepción de un pedido, el cliente puede dejar una reseña (estrellas + comentario
opcional) por cada producto que compró. Se muestran en la ficha del producto, con el promedio y el
total de reseñas.

Dos restricciones, ambas aplicadas en `PedidoService.dejarResena`, no solo en la vista:

- **Solo se reseña un producto de un pedido que llegó de verdad**, y solo si ese producto estaba
  en ese pedido. No se puede reseñar algo que nunca se compró.
- **Una reseña por producto y pedido.** La base tiene una restricción `UNIQUE (pedido_id,
  producto_id)` para que esto no dependa solo de la validación en Java.

## 6. Reporte mensual

*Panel → Reporte mensual* (roles Administrador y Marketing).

1. **Personas atendidas** — clientes registrados distintos + visitantes sin cuenta, con comparativo
   contra el mes anterior, distribución por canal y por tema.
2. **Calificación** — promedio sobre 5, porcentaje de satisfacción (4 y 5 estrellas), distribución
   completa y tasa de casos resueltos.
3. **Recomendaciones de los clientes** — comentarios textuales, ordenados de la peor a la mejor
   calificación: primero lo que exige acción.
4. **Sugerencias para la administración** — generadas por reglas en `ReporteService`, cada una con
   prioridad (Alta / Media / Baja) y el indicador que la origina, para que se pueda verificar.

Ejemplos de reglas que disparan sugerencias: promedio bajo 3.5, más del 30 % de casos escalados, un
tema que concentra más del 25 % de las consultas, caída de más del 15 % en volumen, contactos fuera
de horario, y clientes sin autorización de marketing antes de una campaña.

### Gráficos

El reporte trae un anillo de satisfacción, otro de casos resueltos, barras por tema, distribución de
estrellas, torta por canal y una línea de evolución de los últimos seis meses.

Están dibujados como **SVG generado en el servidor** (`GraficoService`), no con una librería de
JavaScript. La razón es práctica: el SVG viaja dentro del HTML, se ve igual sin conexión y sale bien
al imprimir. Chart.js y similares dibujan sobre un `canvas` que muchas veces sale en blanco en la
impresión, justo cuando más se necesita.

### Descargas

- **PDF** — `ReportePdfService` arma el documento con OpenPDF, construido a mano en vez de convertir
  la página: así no arrastra la maquetación de pantalla y queda con formato de documento archivable.
  Hay PDF para el reporte mensual y para cada orden de pedido (el cliente puede bajar la suya, y solo
  la suya).
- **CSV** para llevar los datos a Excel.
- **Imprimir** o guardar como PDF desde el navegador (la hoja de estilos incluye `@media print`).

### Historiales

*Panel → Historial* muestra todos los pedidos con quién aprobó y quién despachó cada uno. Si el rol
es ADMIN o JEFE, además lista los usuarios registrados — pero solo datos de acceso: usuario, correo y
rol. Los datos personales siguen consultándose desde el directorio, con su política de acceso. El
filtro se aplica **en el controlador**, no en la vista: la información que un rol no puede ver ni
siquiera llega al HTML.

---

## 7. Estructura

```
src/main/java/com/tiendaropa/
├── config/       SecurityConfig, ConfiguracionCifrado, DatosDemo
├── controller/   Home, Auth, MiCuenta, Panel, Atencion, Reporte, ChatApi
├── dto/          ReporteDTO, VistaClienteDTO, Acceso, RegistroForm...
├── model/        Entidades JPA + enums (Rol, NivelDato, Canal, Tema)
├── repository/   Interfaces Spring Data JPA
├── security/     DetalleUsuarioService, RedireccionPorRol
├── service/      ClienteService, ChatbotService, ReporteService,
│                 PoliticaAccesoService, AuditoriaService...
└── util/         CifradoAes, ConvertidorCifrado, Enmascarar

src/main/webapp/
├── WEB-INF/jsp/  Vistas (layout, auth, cliente, panel, error)
└── recursos/     estilos.css, chat.js
```

---

## 7.b Seguridad de la clave en el registro

El formulario muestra en vivo qué tan segura es la clave: una barra de color, el puntaje sobre 100 y
la lista concreta de lo que le falta (longitud, mayúsculas, números, no repetir el usuario, evitar
palabras de listas filtradas).

Además hay un botón **Sugerir** que genera una clave de 14 caracteres y la escribe en el campo, en
claro, con el aviso de copiarla antes de continuar. Dos detalles del diseño:

- Usa `crypto.getRandomValues`, **no `Math.random`**. El segundo es predecible y no sirve para nada
  relacionado con seguridad, aunque a simple vista el resultado parezca igual de aleatorio.
- Excluye caracteres que se confunden al leerlos (`l` con `1`, `O` con `0`). Una clave que se copia
  mal es una clave que el usuario termina cambiando por una débil.

Lo importante es que **la misma evaluación corre en el servidor** (`SeguridadClaveService`), y es esa
la que decide. La del navegador es comodidad: cualquiera puede saltársela enviando el formulario por
otro medio, así que no puede ser la única. Por debajo de 50/100 el registro se rechaza con el motivo
exacto.

## 8. Notas de seguridad

- Claves con **BCrypt**; ni el equipo técnico puede leerlas.
- **CSRF** activo en todos los formularios; solo `/api/chat/**` está excluido por ser JSON.
- Spring Security 6 con `dispatcherTypeMatchers(FORWARD, ERROR)` permitido: sin eso, los *forward*
  internos hacia `/WEB-INF/jsp/**` provocan un bucle de redirecciones en `/login`.
- La frase de cifrado está en `application.properties` solo para desarrollo. En producción va como
  variable de entorno `CLAVE_CIFRADO`. **Si cambias esa frase, los datos ya cifrados dejan de leerse.**
- El personal interno (admin, agente, marketing, oficial) **no** se crea por el registro público.

---

## 9. Qué falta si esto pasa a producción

Vale la pena decirlo con claridad: esto es una base sólida, no un sistema terminado.

- Pasarela de pago real y carrito de compras.
- Correo transaccional (confirmación de pedido, guía de envío, recuperación de clave).
- Imágenes reales de producto (hoy las fichas usan un bloque de color con el SKU).
- Rotación de la llave de cifrado y respaldo cifrado de la base de datos.
- Pruebas automatizadas y HTTPS obligatorio.
