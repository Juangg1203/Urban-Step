# 5. Requerimientos no funcionales

| ID | Requisito |
|---|---|
| RNF-01 | Seguridad: contraseñas con BCrypt, datos privados y sensibles cifrados con AES-256-GCM |
| RNF-02 | Auditoria de todo acceso a datos personales, incluidos los intentos denegados |
| RNF-03 | Disponibilidad del chatbot sin depender de un servicio externo (motor de reglas de respaldo) |
| RNF-04 | Trazabilidad: ningun estado de pedido cambia sin quedar registrado quien y cuando |
| RNF-05 | Usabilidad: mensajes de error explican que paso y que hacer, no solo que algo fallo |
| RNF-06 | Portabilidad: corre con JDK 21 y MySQL/MariaDB, sin dependencias de pago obligatorias |
| RNF-07 | Consistencia financiera: la comision de un vendedor se calcula una sola vez por pedido y se anula si el pedido se cancela, para que nunca quede una comision sin un pedido pagado detras |

## Restricciones
- Estudiante sin comercio registrado en la pasarela de pagos: la integracion funciona en
  un modo simulado que no requiere RUT ni cuenta real.
- Documento y codigo deben poder generarse y verificarse sin herramientas de pago.
