# 01. Análisis

## Actores del sistema

| Actor | Qué necesita del sistema |
|---|---|
| **Cliente** | Comprar sin fricción, saber en qué va su pedido, confiar en que sus datos están protegidos |
| **Empleado (Vendedor)** | Confirmar pagos rápido, sin ver información que no le corresponde |
| **Empleado (Bodeguero)** | Saber qué despachar y con qué guía, sin mezclar eso con la parte de pagos |
| **Jefe** | Controlar qué se vende (catálogo) y dar el visto bueno final a las compras, con trazabilidad |
| **Administrador** | Mantener el sistema funcionando y seguro: cuentas del personal, supervisión general |

## Reglas de negocio identificadas

1. Un cliente no puede comprar más unidades de las que hay en inventario (se valida dos veces:
   al ver el checkout y justo antes de generar el pedido).
2. El inventario se descuenta cuando el pedido se genera, no antes ni después; se repone si el
   pedido se rechaza o se cancela.
3. Un pedido pasa por dos filtros humanos después del pago: el vendedor confirma que el dinero
   entró, el jefe da el visto bueno final antes de que pase a bodega. Ninguno decide por el otro.
4. Cada rol interno ve solo la bandeja de trabajo que le corresponde; esto se aplica en la
   consulta a la base, no solo ocultando botones en la pantalla.
5. Un producto con ventas registradas no se puede eliminar, solo retirar del catálogo (baja
   lógica), para que el historial de pedidos no quede roto.
6. Solo se puede dejar una reseña de un producto que de verdad se compró y llegó, y solo una vez
   por producto y pedido.
7. El administrador no administra el catálogo ni aprueba pedidos: administra las cuentas del
   personal y supervisa. El jefe es quien vende y aprueba.
8. Ningún dato privado o sensible del cliente se expone a un rol que no lo necesite para su
   trabajo (matriz de acceso por nivel de dato).

## Casos de uso principales

Ver `casos-de-uso.png` (diagrama) para la relación completa actor-caso de uso. Resumen de los
flujos centrales:

- **Comprar** (Cliente): explorar catálogo → agregar al carrito → checkout → pagar (pasarela o
  reporte manual con comprobante) → seguir el pedido → confirmar recepción → dejar reseña.
- **Vender** (Jefe): gestionar catálogo (CRUD de productos) → revisar pagos verificados → aceptar
  o rechazar la compra.
- **Despachar** (Empleado-Bodeguero): ver pedidos pagados → alistar → despachar con guía.
- **Cobrar** (Empleado-Vendedor): ver pagos reportados → confirmar que el dinero entró.
- **Administrar personal** (Administrador): crear, editar, activar o desactivar cuentas internas.
- **Atender** (Chatbot / Cliente): consultar el chat 24/7, con datos reales del pedido y del
  catálogo cuando el cliente está autenticado.

## Historias de usuario (muestra representativa)

| Como... | quiero... | para... |
|---|---|---|
| cliente | ver el estado real de mi pedido en el chat | no tener que entrar a Mis pedidos solo para eso |
| cliente | que la referencia de un producto no la escriba yo | no equivocarme ni repetir un código |
| vendedor | ver solo los pagos por confirmar | no perder tiempo revisando lo que le toca a bodega |
| jefe | que el rechazo de una compra exija un motivo | que el cliente entienda por qué |
| administrador | crear la cuenta de un nuevo bodeguero | que empiece a trabajar sin que él mismo se registre |
