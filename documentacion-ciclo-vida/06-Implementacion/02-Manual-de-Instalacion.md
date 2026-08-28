# 2. Manual de instalacion

## Requisitos
- JDK 21
- Maven 3.9 o superior
- MySQL 8 o MariaDB (sirve el de XAMPP)

## Pasos

1. Crear la base de datos ejecutando `04-Desarrollo/database/01_esquema.sql`
   (esquema completo, con todas las tablas y columnas de las fases entregadas,
   incluida la comision de venta).
2. Ajustar usuario y clave de MySQL en `04-Desarrollo/configuracion/application.properties`
   (o en `src/main/resources/application.properties` del proyecto Maven original).
3. Ejecutar `mvn clean spring-boot:run`, o lanzar `TiendaRopaApplication` desde el IDE.
4. Abrir `http://localhost:8080`

El proyecto se empaqueta como **war**, no como jar: es la unica forma de que las vistas
JSP funcionen con el Tomcat embebido de Spring Boot.

## Usuarios de prueba

| Usuario | Clave | Rol |
|---|---|---|
| admin | Admin123 | Administrador |
| jefe | Jefe123 | Jefe |
| vendedor | Vendedor123 | Empleado (Vendedor) |
| bodeguero | Bodeguero123 | Empleado (Bodeguero) |
| laura | Cliente123 | Cliente |

## Si la base ya existia con una version anterior del modelo

Si al entrar al panel aparece un error de tipo `No enum constant...`, la base tiene
datos de una version anterior del esquema. La solucion mas segura:

```sql
DROP DATABASE tienda_ropa;
```

y volver a ejecutar `01_esquema.sql` completo.
