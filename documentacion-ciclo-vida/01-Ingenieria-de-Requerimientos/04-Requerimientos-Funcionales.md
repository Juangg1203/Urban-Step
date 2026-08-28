# 4. Requerimientos funcionales

| ID | Requisito |
|---|---|
| RF-01 | Registro de usuarios (clientes, por formulario publico) |
| RF-02 | Inicio y cierre de sesion |
| RF-03 | Validar credenciales contra la base de datos |
| RF-04 | Contraseñas cifradas, nunca en texto plano |
| RF-05 | Minimo dos roles (Cliente, Administrador); ampliados a cuatro (Cliente, Empleado con subtipo Vendedor/Bodeguero, Jefe, Administrador) |
| RF-06 | Restringir funcionalidades administrativas por rol |
| RF-07 | CRUD de productos (crear, consultar, editar, eliminar) |
| RF-08 | Productos desde base de datos, ninguno escrito en el HTML |
| RF-09 | Cada producto con id, nombre, descripcion, categoria, precio, imagen, cantidad y estado |
| RF-10 | Controlar la cantidad disponible de cada producto |
| RF-11 | Impedir comprar mas cantidad de la disponible |
| RF-12 | Alertar cuando un producto llega al nivel minimo de existencias |
| RF-13 | Actualizar el inventario cuando se genera un pedido |
| RF-14 | Carrito de compras |
| RF-15 | Generacion y gestion de pedidos, con estados |
| RF-16 | Panel administrativo con indicadores del negocio |
| RF-17 | Internacionalizacion (español / ingles) |
| RF-18 | Modelo de datos y diagrama entidad-relacion documentados |
| RF-19 | Arquitectura documentada |

## Requisitos ampliados durante el desarrollo

| ID | Requisito | Origen |
|---|---|---|
| RA-01 | Chatbot de atencion, con reglas y datos reales de pedidos/catalogo | Peticion del estudiante |
| RA-02 | Reporte mensual con graficos y sugerencias automaticas | Peticion del estudiante |
| RA-03 | Pago en linea (Wompi, con modo simulado) | Peticion del estudiante |
| RA-04 | Flujo de pedido con doble verificacion humana | Peticion del estudiante |
| RA-05 | Resenas de producto | Peticion del estudiante |
| RA-06 | Referencia (SKU) autogenerada por categoria | Peticion del estudiante |
| RA-07 | Gestion de personal interno por el Administrador | Correccion de alcance del rol |
| RA-08 | Rediseño visual bajo la marca UrbanStep | Peticion del estudiante |
| RA-09 | Comision de venta para el vendedor, por producto vendido, activada por el cliente en el sitio o por una venta asistida por el vendedor | Peticion del estudiante |
