# UrbanStep

Sistema de informacion empresarial para una tienda de ropa y calzado urbano.
Proyecto de la asignatura Desarrollo de Aplicaciones Empresariales.

Documentado siguiendo el ciclo de vida completo del desarrollo de software:

| Fase | Contenido |
|---|---|
| [01-Ingenieria-de-Requerimientos](01-Ingenieria-de-Requerimientos/) | Enunciado original, requisitos, historias de usuario, reglas de negocio, matriz de trazabilidad |
| [02-Analisis](02-Analisis/) | Actores, casos de uso, flujos de proceso, modelo de dominio |
| [03-Diseno](03-Diseno/) | Arquitectura, clases, secuencia, componentes, modelo de base de datos, diccionario de datos, interfaces, seguridad |
| [04-Desarrollo](04-Desarrollo/) | Codigo fuente organizado por capas (backend, frontend, database, configuracion) |
| [05-Pruebas](05-Pruebas/) | Plan de pruebas, 28 casos de prueba, pruebas de integracion y usabilidad, matriz de trazabilidad |
| [06-Implementacion](06-Implementacion/) | Manual de instalacion, configuracion de produccion, despliegue, manual de usuario, mantenimiento |

## Funcionalidad destacada: comision de venta

El vendedor puede ganar comision de dos formas: el cliente lo elige en el checkout, o
el propio vendedor arma la compra por un cliente presencial ("venta asistida"). La
comision se calcula por producto (cada uno tiene su propio porcentaje, definido por el
Jefe) y solo se confirma cuando el pedido llega de verdad al cliente. Ver el detalle en
`01-Ingenieria-de-Requerimientos/08-Reglas-de-Negocio.md` y en
`02-Analisis/04-Flujos-de-Proceso.md`.

## Proyecto ejecutable

El codigo fuente compilable (proyecto Maven completo, listo para `mvn spring-boot:run`)
se entrega junto a esta documentacion. Ver `06-Implementacion/02-Manual-de-Instalacion.md`
para ponerlo en marcha.
