# 3. Casos de uso

Ver `05-Diagrama-Casos-de-Uso.png` para la relacion completa actor-caso de uso.

## Comprar (Cliente)
Explorar catalogo -> agregar al carrito -> checkout (elegir si un vendedor lo atendio) ->
pagar -> seguir el pedido -> confirmar recepcion -> dejar resena.

## Vender de forma asistida (Vendedor) — caso de uso nuevo
El vendedor busca al cliente por su usuario o correo, arma el carrito por el, y confirma
la compra en su nombre. El pedido nace ya asociado a ese vendedor para la comision, sin
que se le pregunte nada al cliente.

## Vender (Jefe)
Gestionar catalogo (con el porcentaje de comision de cada producto) -> revisar pagos
verificados -> aceptar o rechazar la compra.

## Despachar (Bodeguero)
Ver pedidos pagados -> alistar -> despachar con guia.

## Cobrar (Vendedor)
Ver pagos reportados -> confirmar que el dinero entro -> consultar sus comisiones.

## Administrar personal (Administrador)
Crear, editar, activar o desactivar cuentas internas.
