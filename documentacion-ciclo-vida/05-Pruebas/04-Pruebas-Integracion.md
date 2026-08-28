# 4. Pruebas de integracion

Estas pruebas cruzan varios modulos a la vez; no basta con que cada pieza funcione
aislada, tiene que funcionar la cadena completa.

| ID | Cadena que se prueba | Modulos involucrados |
|---|---|---|
| PI-01 | Checkout -> Inventario -> Notificaciones | El pedido descuenta el stock correcto y avisa al rol correspondiente en cada paso |
| PI-02 | Pago (pasarela simulada) -> PedidoService -> Notificaciones | Un pago aprobado mueve el pedido y notifica al vendedor |
| PI-03 | Aceptacion del jefe -> Inventario -> Comision -> Notificaciones | Al aceptar: no se vuelve a tocar inventario (ya se habia descontado), se calcula la comision, se avisa a bodega |
| PI-04 | Venta asistida -> Carrito de sesion -> Pedido -> Comision | El carrito del vendedor (como comprador) y el de la venta asistida no se mezclan |
| PI-05 | Cancelacion administrativa -> Inventario -> Comision | Un cambio manual de estado revierte inventario y anula comision en la misma operacion |
| PI-06 | Resena -> Pedido -> Producto | La resena solo se habilita si el pedido especifico llego a ENTREGADO |
| PI-07 | Chatbot -> PedidoService -> ProductoService | El chatbot consulta datos reales, no una respuesta fija, cuando el cliente pregunta por su pedido o el catalogo |
