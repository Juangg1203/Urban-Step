# 8. Reglas de negocio

1. Un cliente no puede comprar mas unidades de las que hay en inventario.
2. El inventario se descuenta cuando el pedido se genera, no antes ni despues; se repone
   si el pedido se rechaza o se cancela.
3. Un pedido pasa por dos filtros humanos despues del pago: el vendedor confirma que el
   dinero entro, el jefe da el visto bueno final antes de que pase a bodega.
4. Cada rol interno ve solo la bandeja de trabajo que le corresponde.
5. Un producto con ventas registradas no se puede eliminar, solo retirar del catalogo.
6. Solo se puede dejar una resena de un producto que de verdad se compro y llego, una
   sola vez por producto y pedido.
7. El administrador no administra el catalogo ni aprueba pedidos: administra las cuentas
   del personal y supervisa. El jefe es quien vende y aprueba.
8. Ningun dato privado o sensible del cliente se expone a un rol que no lo necesite.
9. **Comision de venta**: un pedido queda asociado a un vendedor de dos formas posibles,
   nunca las dos a la vez — el cliente lo elige en el checkout, o el vendedor hace la
   venta asistida por el. La comision se calcula por producto (cada producto tiene su
   propio porcentaje, definido por el Jefe) y se consolida solo cuando el pago del
   pedido queda confirmado, no antes: una compra que nunca se pago no genera comision.
10. Si el pedido asociado a un vendedor se cancela o es rechazado, la comision que se
    habia calculado se anula junto con el pedido.
