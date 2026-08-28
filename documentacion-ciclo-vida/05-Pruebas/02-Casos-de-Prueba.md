# 2. Casos de prueba

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| CP-01 | Registro con clave debil | Ir a /registro, usar clave "123456" | El servidor rechaza el registro y explica por que |
| CP-02 | Registro con clave fuerte | Repetir con clave que pase el medidor | Cuenta creada, redirige a login |
| CP-03 | Login con credenciales erroneas | Entrar con clave incorrecta | Mensaje de error, no revela si el usuario existe |
| CP-04 | Cifrado de contraseña | Consultar `usuario` en MySQL tras CP-02 | El campo `clave` empieza por `$2a$` (BCrypt) |
| CP-05 | CRUD de productos | Como Jefe, crear producto sin categoria | El formulario exige categoria antes de generar el SKU |
| CP-06 | SKU automatico | Crear dos camisetas seguidas | La segunda queda con un numero mas que la anterior |
| CP-07 | Relleno de huecos en SKU | Eliminar un producto sin ventas, crear uno nuevo de la misma categoria | El nuevo ocupa el numero libre |
| CP-08 | Restriccion de rol: productos | Como Administrador, intentar ir a /panel/productos | Acceso denegado (403) |
| CP-09 | Limite de inventario | Agregar al carrito mas unidades de las que hay | No deja completar la compra, explica cuanto hay |
| CP-10 | Alerta de stock bajo | Poner el stock de un producto bajo el minimo | Aparece en el panel del Jefe |
| CP-11 | Flujo de pedido completo | Cliente compra -> Vendedor confirma -> Jefe acepta -> Bodega despacha -> Cliente confirma | Cada paso mueve el estado, con nombre y fecha |
| CP-12 | Silo de bandejas | Como Vendedor, ir a /panel/pedidos | Solo aparece la cola de pagos |
| CP-13 | Rechazo sin motivo | Como Jefe, rechazar sin escribir motivo | El sistema exige el motivo |
| CP-14 | Reposicion de inventario | Rechazar un pedido ya pagado | Las unidades vuelven al stock |
| CP-15 | Resena sin haber recibido | Dejar resena de un pedido no entregado | La accion se rechaza |
| CP-16 | Resena duplicada | Dejar la resena dos veces | La segunda no se guarda |
| CP-17 | Pago simulado rechazado | En la pasarela simulada, elegir rechazo | El pedido vuelve a pendiente de pago |
| CP-18 | Verificacion de firma | Alterar el monto del pago desde el navegador | La pasarela rechaza por firma invalida |
| CP-19 | Auditoria | Como Empleado, intentar ver un dato privado ajeno | El intento queda en `log_auditoria` |
| CP-20 | Gestion de usuarios | Como Admin, crear vendedor con clave debil | Se rechaza con el mismo medidor del registro publico |
| CP-21 | Chatbot con datos reales | Logueado, preguntar por el pedido | Responde con el numero y estado real |
| CP-22 | Internacionalizacion | Cambiar a ingles | Catalogo, carrito y checkout cambian; el panel interno no |
| **CP-23** | **Comision: cliente elige vendedor** | En el checkout, elegir un vendedor de la lista | El pedido queda asociado a ese vendedor |
| **CP-24** | **Comision: se calcula al aceptar** | Jefe acepta un pedido con vendedor asociado | `comision_monto` se llena, `comision_estado` pasa a PENDIENTE |
| **CP-25** | **Comision: se confirma al entregar** | Cliente confirma que recibio el pedido | `comision_estado` pasa a CONFIRMADA |
| **CP-26** | **Comision: se anula si se cancela** | Admin fuerza el pedido a CANCELADO despues de tener comision | `comision_estado` pasa a ANULADA |
| **CP-27** | **Venta asistida** | Como Vendedor, buscar un cliente y comprar por el | El pedido se crea a nombre del cliente, con el vendedor como referido |
| **CP-28** | **Comision por producto** | Como Jefe, poner 10% de comision a un producto, venderlo por $100.000 | La comision de esa linea es $10.000 |
