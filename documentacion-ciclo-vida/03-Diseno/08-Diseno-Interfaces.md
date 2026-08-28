# 8. Diseño de interfaces

## Identidad visual (marca UrbanStep)

| Elemento | Decision |
|---|---|
| Paleta | Fondo oscuro tipo asfalto (#0B0E14) con acentos neon: lima (accion/marca), cian (informacion), magenta (alerta/rechazo), violeta (categorias), ambar (pendiente) |
| Tipografia | Archivo Black (titulos), Space Grotesk (cuerpo), JetBrains Mono (datos y codigos) |
| Interaccion | Los elementos que se pueden pulsar se levantan y encienden un halo de su color al pasar el cursor; lo que no es accionable no se mueve |
| Logo | Aplicado en cabecera, pie, login, registro y favicon |

## Pantallas principales

| Pantalla | Rol | Elementos clave |
|---|---|---|
| Catalogo | Publico | Filtro por linea y categoria, tarjetas con imagen real |
| Checkout | Cliente | Pais/ciudad en desplegable, direccion libre, **selector opcional de vendedor que lo atendio** |
| Venta asistida | Vendedor | Buscador de cliente + armado de carrito a su nombre |
| Mis comisiones | Vendedor | Total ganado, pendiente, historial por pedido |
| Panel | Segun rol | Accesos primero, indicadores despues |
| Aprobaciones | Jefe | Cola de pedidos con pago verificado, esperando visto bueno |

## Principio de diseño de formularios
Todo formulario valida en el servidor ademas de en el navegador; los mensajes de error
dicen que paso y que hacer, no solo que algo fallo.
