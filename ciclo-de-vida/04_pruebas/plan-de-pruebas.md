# 04. Pruebas

No hay pruebas automatizadas (JUnit) en este proyecto — es una limitación reconocida, ver
la nota al final. Lo que sigue es el **plan de pruebas manuales**, con casos trazables a
requisitos concretos. Cada caso indica cómo ejecutarlo y qué resultado esperar; al
correrlos, se anota el resultado real y se anexa la captura correspondiente en esta misma
carpeta (`capturas/`).

## Casos de prueba

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| CP-01 | Registro de cliente | Ir a /registro, llenar el formulario con una clave débil (ej. "123456") | El servidor rechaza el registro y explica por qué |
| CP-02 | Registro con clave fuerte | Repetir CP-01 con una clave que pase el medidor | Cuenta creada, redirige a login |
| CP-03 | Login con credenciales erróneas | Entrar con una clave incorrecta | Mensaje de error, no se revela si el usuario existe |
| CP-04 | Cifrado de contraseña | Consultar la tabla `usuario` en MySQL tras CP-02 | El campo `clave` empieza por `$2a$` (BCrypt), nunca el texto original |
| CP-05 | CRUD de productos | Como Jefe, crear un producto sin elegir categoría | El formulario exige categoría antes de generar el SKU |
| CP-06 | SKU automático | Crear dos camisetas seguidas | La segunda queda como CAM-00X, un número más que la anterior |
| CP-07 | Relleno de huecos en SKU | Eliminar un producto sin ventas, crear uno nuevo de la misma categoría | El nuevo ocupa el número que quedó libre |
| CP-08 | Restricción de rol: productos | Entrar como Administrador, intentar ir a /panel/productos | Acceso denegado (403) |
| CP-09 | Límite de inventario | Agregar al carrito más unidades de las que hay en stock | El sistema no deja completar la compra, explica cuánto hay disponible |
| CP-10 | Alerta de stock bajo | Editar un producto y poner el stock por debajo del mínimo | Aparece en el panel del Jefe, en el bloque de alerta |
| CP-11 | Flujo de pedido completo | Cliente compra → Vendedor confirma pago → Jefe acepta → Bodeguero despacha → Cliente confirma recepción | Cada paso mueve el estado correctamente y queda con nombre y fecha |
| CP-12 | Silo de bandejas | Entrar como Vendedor, ir a /panel/pedidos | Solo aparece la cola de pagos, nunca la de despachos |
| CP-13 | Rechazo de compra | Como Jefe, rechazar un pedido sin escribir motivo | El sistema exige el motivo antes de guardar |
| CP-14 | Reposición de inventario | Rechazar un pedido ya pagado | Las unidades vuelven al stock del producto |
| CP-15 | Reseña sin haber recibido | Intentar dejar una reseña de un pedido que no está en estado Entregado | La acción se rechaza |
| CP-16 | Reseña duplicada | Dejar una reseña dos veces sobre el mismo producto y pedido | La segunda vez no se guarda |
| CP-17 | Pago simulado rechazado | En la pasarela simulada, elegir "El banco lo rechaza" | El pedido vuelve a pendiente de pago, el cliente puede reintentar |
| CP-18 | Verificación de firma | Alterar el monto en el formulario de pago antes de enviarlo (herramientas del navegador) | La pasarela rechaza la transacción por firma inválida |
| CP-19 | Auditoría | Como Empleado, intentar ver un dato privado que no le corresponde | El intento queda registrado en `log_auditoria` como acceso denegado |
| CP-20 | Gestión de usuarios | Como Administrador, crear un vendedor con clave débil | El sistema rechaza la clave con el mismo medidor que el registro público |
| CP-21 | Chatbot con datos reales | Estando logueado, preguntarle al chat por el pedido | Responde con el número y estado real del último pedido, no un texto genérico |
| CP-22 | Internacionalización | Cambiar el idioma a inglés desde cualquier página | El catálogo, el carrito y el checkout cambian de idioma; el panel interno no |

## Evidencia

Las capturas de pantalla que respaldan estos casos van en `capturas/`, nombradas con el ID
del caso (ej. `CP-11-flujo-completo.png`).

## Limitación reconocida

Este proyecto no incluye pruebas unitarias automatizadas (JUnit/Mockito). Dado el tamaño del
alcance final, sería la siguiente inversión razonable antes de llevar el proyecto a
producción — sobre todo para `PedidoService`, que concentra la lógica de negocio más
sensible (inventario, estados, dinero).
