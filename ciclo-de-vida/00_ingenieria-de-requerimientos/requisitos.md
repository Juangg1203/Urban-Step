# 00. Ingeniería de requerimientos

Fuente: `enunciado-original.pdf` (actividad de la asignatura Desarrollo de Aplicaciones
Empresariales). Aquí se traducen esos requisitos a una lista verificable, con el
identificador que se usa en el resto de la documentación y en el código.

## Requisitos funcionales

| ID | Requisito | Prioridad |
|---|---|---|
| RF-01 | Registro de usuarios (clientes, por formulario público) | Alta |
| RF-02 | Inicio y cierre de sesión | Alta |
| RF-03 | Validar credenciales contra la base de datos | Alta |
| RF-04 | Contraseñas cifradas, nunca en texto plano | Alta |
| RF-05 | Mínimo dos roles (Cliente, Administrador); se ampliaron a cuatro (Cliente, Empleado con subtipo Vendedor/Bodeguero, Jefe, Administrador) | Alta |
| RF-06 | Restringir funcionalidades administrativas por rol | Alta |
| RF-07 | CRUD de productos (crear, consultar, editar, eliminar) | Alta |
| RF-08 | Productos desde base de datos, ninguno escrito en el HTML | Alta |
| RF-09 | Cada producto con id, nombre, descripción, categoría, precio, imagen, cantidad y estado | Alta |
| RF-10 | Controlar la cantidad disponible de cada producto | Alta |
| RF-11 | Impedir comprar más cantidad de la disponible | Alta |
| RF-12 | Alertar cuando un producto llega al nivel mínimo de existencias | Media |
| RF-13 | Actualizar el inventario cuando se genera un pedido | Alta |
| RF-14 | Carrito de compras | Alta |
| RF-15 | Generación y gestión de pedidos, con estados | Alta |
| RF-16 | Panel administrativo con indicadores del negocio (usuarios, productos, inventario bajo, pedidos, últimos pedidos, estado de los pedidos) | Alta |
| RF-17 | Internacionalización (español / inglés) | Media |
| RF-18 | Modelo de datos y diagrama entidad-relación documentados | Alta |
| RF-19 | Arquitectura documentada | Media |

## Requisitos ampliados durante el desarrollo

Estos no estaban en el enunciado original; se agregaron a pedido del estudiante y quedan
documentados aquí porque cambian el alcance del sistema:

| ID | Requisito | Origen |
|---|---|---|
| RA-01 | Chatbot de atención al cliente, con reglas y modelo de lenguaje opcional | Petición del estudiante |
| RA-02 | Reporte mensual de atención, con gráficos y sugerencias automáticas | Petición del estudiante |
| RA-03 | Pago en línea (pasarela Wompi, con modo simulado) | Petición del estudiante |
| RA-04 | Flujo de pedido con doble verificación humana (vendedor confirma pago, jefe da el visto bueno) | Petición del estudiante |
| RA-05 | Reseñas de producto, atadas a un pedido entregado | Petición del estudiante |
| RA-06 | Referencia de producto (SKU) autogenerada por categoría | Petición del estudiante |
| RA-07 | Gestión de personal interno por el Administrador (crear/editar/desactivar cuentas) | Corrección de alcance del rol Administrador |
| RA-08 | Rediseño visual completo bajo la marca UrbanStep | Petición del estudiante |

## Requisitos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | Seguridad: contraseñas con BCrypt, datos privados y sensibles cifrados con AES-256-GCM |
| RNF-02 | Auditoría de todo acceso a datos personales, incluidos los intentos denegados |
| RNF-03 | Disponibilidad del chatbot sin depender de un servicio externo (motor de reglas como respaldo) |
| RNF-04 | Trazabilidad: ningún estado de pedido cambia sin quedar registrado quién y cuándo |
| RNF-05 | Usabilidad: mensajes de error explican qué pasó y qué hacer, no solo que algo falló |
| RNF-06 | Portabilidad: el proyecto corre con JDK 21 y MySQL/MariaDB, sin dependencias de pago obligatorias |

## Restricciones

- Estudiante sin comercio registrado en la pasarela de pagos: la integración debe funcionar en
  un modo simulado que no requiera RUT ni cuenta real.
- Documento y código deben poder generarse y verificarse sin herramientas de pago.
