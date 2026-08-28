# 02. Diseño

## Diseño de datos
Ver `diagrama-entidad-relacion.png` / `.svg`. 16 tablas agrupadas en cuatro bloques:
núcleo del negocio, usuarios y datos personales, atención al cliente, apoyo y control.
Detalle de las decisiones de modelado en el documento de funcionalidades, sección 3.

## Diseño de arquitectura
Ver `diagrama-arquitectura.png` / `.svg`. Monolito en capas sobre Spring Boot 3:
presentación (JSP) → seguridad → controladores → servicios → repositorios → MySQL.
Se eligió monolito sobre una API separada porque el proyecto lo mantiene un equipo
pequeño y no necesita servir a varios clientes distintos.

## Diseño visual (marca UrbanStep)

| Elemento | Decisión |
|---|---|
| Paleta | Fondo oscuro tipo asfalto (`#0B0E14`) con acentos neón: lima (acción/marca), cian (información), magenta (alerta/rechazo), violeta (categorías), ámbar (pendiente) |
| Tipografía | Archivo Black (títulos), Space Grotesk (cuerpo), JetBrains Mono (datos y códigos) |
| Interacción | Los elementos que se pueden pulsar se levantan y encienden un halo de su color al pasar el cursor; lo que no es accionable no se mueve, para que el gesto siga significando algo |
| Logo | Aplicado en cabecera, pie, login, registro y favicon |

## Diseño de la matriz de acceso a datos

| Nivel | Público | Semiprivado | Privado | Sensible |
|---|---|---|---|---|
| Quién ve | Todos los roles internos | Empleado, Jefe, Admin | Titular y Jefe completo; Empleado enmascarado | Solo el titular |
| Dónde vive | `cliente` | `compra_cliente`, `pedido` | `dato_privado_cliente` (cifrado) | `dato_sensible_cliente` (cifrado) |

Centralizada en una sola clase (`PoliticaAccesoService`): si cambia la política, se cambia
en un solo lugar en vez de en cada controlador.

## Diseño de interfaz
Las capturas de pantalla de las vistas finales (catálogo, checkout, panel por rol) se anexan
en la carpeta 04_pruebas, como evidencia de las pruebas de aceptación — ahí demuestran
tanto el diseño como el funcionamiento real.
