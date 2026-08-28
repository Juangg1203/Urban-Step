# 9. Seguridad

- Contraseñas cifradas con BCrypt; nunca se guardan ni se muestran en texto plano.
- Datos privados y sensibles del cliente cifrados con AES-256-GCM, en una tabla aparte
  del resto del registro.
- CSRF activo en todos los formularios.
- Matriz de acceso centralizada (`PoliticaAccesoService`): un solo lugar decide quien ve
  que nivel de dato.
- Auditoria de todo acceso a datos personales, incluidos los intentos denegados.
- La firma del pago (Wompi) se calcula en el servidor; si alguien altera el monto desde
  el navegador, la pasarela rechaza la transaccion.
- Un pedido ajeno responde igual que uno inexistente, para no confirmar por descarte a
  quien pertenece.
- **Comision de venta**: la asociacion de un pedido con un vendedor se fija al crear el
  pedido y no se puede reasignar despues desde la interfaz — evita que alguien reasigne
  comisiones ya generadas. Solo el Administrador puede forzar un cambio de estado
  manual, y ese cambio queda en la auditoria con motivo obligatorio.
