# 6. Mantenimiento

## Tareas periodicas sugeridas
- Revisar `log_auditoria` en busca de patrones de acceso denegado repetidos.
- Verificar productos con inventario bajo (el panel ya alerta, pero conviene un
  seguimiento manual semanal mientras no haya reabastecimiento automatico).
- Respaldar la base de datos con la frecuencia que el negocio necesite.
- Revisar comisiones pendientes vs. confirmadas, para que el area contable las pueda
  liquidar con datos actualizados.

## Cambios de esquema futuros
Cada cambio de esquema debe ir acompañado de un script `ALTER TABLE` nuevo (siguiendo
el patron ya usado en `01_esquema.sql`), nunca modificando las tablas existentes a
mano en produccion sin dejar el script correspondiente en el repositorio.

## Extensiones razonables a futuro
- Pruebas automatizadas (JUnit/Mockito), empezando por `PedidoService`.
- Recuperacion de clave por correo electronico.
- Integracion real con una transportadora para el numero de guia.
- Reporte especifico de comisiones por vendedor y por periodo, descargable en PDF,
  igual que ya existe para el reporte mensual de atencion.
