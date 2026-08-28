# 3. Configuracion para produccion

Este apartado documenta que cambiaria si el proyecto saliera de un entorno academico
a uno real. No es lo que esta configurado hoy por defecto.

| Aspecto | En desarrollo (actual) | En produccion |
|---|---|---|
| Clave de cifrado (`app.cifrado.clave`) | Valor fijo en el archivo | Variable de entorno, nunca en el repositorio |
| Pasarela de pagos | `app.pago.wompi.modo=simulado` | `modo=real`, con las llaves del comercio en variables de entorno |
| Base de datos | Usuario root sin clave | Usuario dedicado, con permisos minimos, clave fuerte |
| IA del chatbot | Deshabilitada por defecto | Llave de API en variable de entorno si se activa |
| `spring.jpa.hibernate.ddl-auto` | `update` (comodo para desarrollo) | Deberia pasar a `validate`, con las migraciones controladas aparte |
| HTTPS | No configurado (localhost) | Obligatorio, con certificado valido |
| Logs | Nivel INFO general | Restringir informacion sensible en los logs de produccion |
