# 05. Implementación — manual de instalación

## Requisitos
- JDK 21
- Maven 3.9 o superior
- MySQL 8 o MariaDB (sirve el de XAMPP)

## Pasos

1. Crear la base de datos ejecutando `basedatos/01_esquema.sql` (esquema completo, con
   todas las tablas y columnas de las fases entregadas).
2. Ajustar usuario y clave de MySQL en `src/main/resources/application.properties`.
3. Ejecutar `mvn clean spring-boot:run`, o lanzar `TiendaRopaApplication` desde el IDE.
4. Abrir `http://localhost:8080`

El proyecto se empaqueta como **war**, no como jar: es la única forma de que las vistas
JSP funcionen con el Tomcat embebido de Spring Boot.

## Configuración opcional

| Función | Cómo activarla |
|---|---|
| IA del chatbot (Gemini u Ollama) | `app.chatbot.ia-habilitada=true` en `application.properties` |
| Pago en línea (Wompi real) | `app.pago.wompi.modo=real` + llaves del comercio |
| Pago en línea (simulado, sin cuenta) | `app.pago.wompi.modo=simulado` (activo por defecto) |

## Usuarios de prueba

| Usuario | Clave | Rol |
|---|---|---|
| admin | Admin123 | Administrador |
| jefe | Jefe123 | Jefe |
| vendedor | Vendedor123 | Empleado (Vendedor) |
| bodeguero | Bodeguero123 | Empleado (Bodeguero) |
| laura | Cliente123 | Cliente |

Los datos de demostración (catálogo con fotos reales, usuarios, pedidos en varios estados,
tres meses de atenciones) se cargan solo la primera vez que arranca la aplicación.

## Si la base ya existía con una versión anterior del modelo

El proyecto cambió de esquema varias veces durante el desarrollo (nuevos estados de pedido,
tabla de reseñas, columnas de pasarela). Si al entrar al panel aparece un error de tipo
`No enum constant...`, la base tiene datos de una versión anterior. La solución más segura
es recrearla desde cero:

```sql
DROP DATABASE tienda_ropa;
```

y volver a ejecutar `01_esquema.sql` completo.
