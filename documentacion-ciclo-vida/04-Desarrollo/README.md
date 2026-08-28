# 04. Desarrollo

Esta fase, a diferencia de las demas, no duplica archivos: el codigo fuente real esta
un nivel arriba, en la raiz de este mismo repositorio (`src/main/java`,
`src/main/webapp`, `pom.xml`), porque es el mismo proyecto Maven que se entrega para
compilar y ejecutar. Duplicarlo aqui adentro solo generaria dos copias que se
desincronizarian con cada cambio.

| Esta fase corresponde a... | Ruta real en el repositorio |
|---|---|
| Backend (modelo, servicios, controladores, seguridad) | `/src/main/java/com/tiendaropa/` |
| Frontend (vistas JSP, CSS, JavaScript, imagenes) | `/src/main/webapp/` |
| Base de datos (esquema completo) | `/basedatos/` |
| Configuracion (`application.properties`, `pom.xml`, idiomas) | `/src/main/resources/` y la raiz del repositorio |

## Stack

Spring Boot 3.3, Java 21, JSP + JSTL, MySQL 8/MariaDB, Spring Security 6, BCrypt,
AES-256-GCM, OpenPDF, Bootstrap 5.3 + CSS propio.

## Convenciones de codigo

- Paquetes por responsabilidad: `model`, `repository`, `service`, `controller`, `dto`,
  `util`, `config`, `security`.
- Toda regla de negocio vive en la capa de servicio, nunca en el controlador ni en la
  vista.
- Un pedido nunca cambia de estado sin que quede registrado quien lo hizo — es la regla
  de trazabilidad que atraviesa todo `PedidoService`.
