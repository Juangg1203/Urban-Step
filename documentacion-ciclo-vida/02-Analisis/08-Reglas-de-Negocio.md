# 8. Reglas de negocio (vista de analisis)

Ver el detalle completo en `01-Ingenieria-de-Requerimientos/08-Reglas-de-Negocio.md`.
Aqui se listan solo las que afectan directamente el analisis de casos de uso:

- Un pedido se asocia a **un solo vendedor como maximo**, nunca a mas de uno.
- La asociacion con el vendedor se define en el momento de crear el pedido y **no cambia
  despues**, incluso si otro vendedor es quien confirma el pago.
- La comision se calcula por linea del pedido (producto x cantidad x % de comision),
  nunca sobre el total del pedido en bloque, porque cada producto puede tener un
  porcentaje distinto.
- La venta asistida no le pide clave del cliente: el vendedor busca la cuenta por
  usuario o correo y arma el pedido a nombre del cliente, pero la sesion sigue siendo
  la del vendedor en todo momento.
