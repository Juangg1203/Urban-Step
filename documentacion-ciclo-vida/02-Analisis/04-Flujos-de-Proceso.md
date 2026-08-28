# 4. Flujos de proceso

## Flujo principal: compra con comision de venta

```
Cliente entra al checkout
      |
      v
¿Un vendedor lo atendio? --- No ---> Pedido sin vendedor asociado
      |
     Si (elige de una lista)
      |
      v
Pedido queda con vendedor_id = el elegido
      |
      v
Pago (pasarela o manual) --> PAGO_EN_VERIFICACION
      |
      v
Vendedor confirma el pago --> PENDIENTE_ACEPTACION_JEFE
      |                              (si el vendedor asociado es el mismo
      |                               que confirma, igual gana comision:
      |                               confirmar el pago no es lo mismo
      |                               que haber referido la venta)
      v
Jefe acepta --> PAGADO
      |
      v
Se calcula la comision: suma de (subtotal de cada linea x % del producto)
      |
      v
Bodeguero alista y despacha --> DESPACHADO
      |
      v
Cliente confirma recepcion --> ENTREGADO (puede dejar resena)
```

## Flujo alterno: venta asistida por el vendedor

```
Vendedor entra a "Venta asistida"
      |
      v
Busca al cliente (usuario o correo)
      |
      v
Arma el carrito por el cliente
      |
      v
Confirma la compra
      |
      v
Pedido se genera con vendedor_id = el mismo vendedor,
directo en PENDIENTE_PAGO (como cualquier pedido)
      |
      v
El resto del flujo sigue igual (pago, verificacion, aceptacion, despacho)
```

## Flujo de rechazo
Si el Jefe rechaza el pedido despues del pago, se repone el inventario y, si ya se habia
calculado una comision, se anula junto con el pedido.
