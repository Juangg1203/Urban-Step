# 7. Criterios de aceptacion

## HU-04 / HU-06 — Comision de venta
- Dado que un cliente esta en el checkout, cuando marca que un vendedor lo atendio,
  entonces el pedido queda asociado a ese vendedor.
- Dado que un vendedor abre "Venta asistida", cuando arma un carrito por un cliente y lo
  confirma, entonces el pedido queda asociado a el mismo, sin que se lo pregunten.
- Dado que un pedido con vendedor asociado llega a estado Pagado, cuando se calcula la
  comision, entonces es la suma de (subtotal de cada linea x porcentaje de comision del
  producto), nunca un valor negativo ni superior al total del pedido.
- Dado que un pedido se cancela o se rechaza, cuando ya tenia comision calculada, entonces
  esa comision se anula.

## HU-01 — Registro seguro
- Dado un registro con clave debil, cuando se envia el formulario, entonces el servidor
  lo rechaza con el motivo, sin importar si el navegador ya habia validado.

## HU-08 — Comision por producto
- Dado que el Jefe crea o edita un producto, cuando define el porcentaje de comision,
  entonces ese valor queda entre 0% y 100%, y aplica a partir de ese momento.
