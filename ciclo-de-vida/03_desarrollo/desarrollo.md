# 03. Desarrollo

El código fuente completo vive en la raíz del proyecto (`src/`), no duplicado aquí, para
que el repositorio tenga una sola fuente de verdad y el proyecto siga siendo compilable
con Maven sin mover nada.

## Stack

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.3, Java 21 |
| Vistas | JSP + JSTL, renderizadas en el servidor |
| Base de datos | MySQL 8 / MariaDB |
| Seguridad | Spring Security 6, BCrypt, AES-256-GCM |
| Estilos | CSS propio + Bootstrap 5.3 |
| PDF | OpenPDF |
| Pagos | Wompi (modo real y modo simulado) |

## Convenciones de código

- Paquetes por responsabilidad: `model`, `repository`, `service`, `controller`, `dto`,
  `util`, `config`, `security`.
- Toda regla de negocio vive en la capa de servicio, nunca en el controlador ni en la vista.
- Los comentarios explican **por qué** se tomó una decisión, no qué hace la línea siguiente.
- Un pedido nunca cambia de estado sin que quede registrado quién lo hizo — es la regla de
  trazabilidad que atraviesa todo `PedidoService`.

## Cómo se construyó (fases de desarrollo)

1. Base: catálogo, autenticación, niveles de datos cifrados.
2. Carrito, pedidos y aprobación.
3. Reportes en PDF y con gráficos.
4. Seguridad de contraseñas e internacionalización.
5. CRUD de productos con imagen e inventario.
6. Panel administrativo con los seis indicadores.
7. Pasarela de pagos (real y simulada).
8. Rediseño del flujo de compra: pago directo, doble verificación, reseñas.
9. Gestión de usuarios internos por el administrador.
10. Identidad visual UrbanStep.

El código fuente completo, listo para compilar, está en la raíz de este proyecto.
