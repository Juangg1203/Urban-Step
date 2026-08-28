# 1. Plan de pruebas

## Alcance
Pruebas manuales sobre los modulos criticos: autenticacion, catalogo, inventario,
carrito y pedidos, comision de venta, chatbot, pago en linea, internacionalizacion.

## Tipos de prueba realizadas
- Funcionales (cada requisito por separado) — ver `03-Pruebas-Funcionales.md`
- De integracion (flujos que cruzan varios modulos) — ver `04-Pruebas-Integracion.md`
- De usabilidad (formularios, mensajes de error) — ver `05-Pruebas-Usabilidad.md`

## Limitacion reconocida
No hay pruebas automatizadas (JUnit/Mockito). Es la siguiente inversion razonable antes
de llevar el proyecto a produccion, sobre todo en `PedidoService`, que concentra la
logica de negocio mas sensible (inventario, estados, dinero, comisiones).

## Criterio de aceptacion general
Un caso de prueba pasa si el resultado observado coincide con el esperado Y queda
evidencia (captura) en `evidencias/`.
