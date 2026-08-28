# 4. Despliegue

## Empaquetado
```
mvn clean package
```
Genera `target/tienda-ropa.war`.

## Opciones de despliegue
- **Tomcat embebido** (la usada en desarrollo): `mvn spring-boot:run`, o ejecutar el
  war con `java -jar target/tienda-ropa.war` (el war generado por Spring Boot es
  ejecutable).
- **Tomcat externo**: copiar el `.war` a la carpeta `webapps/` de un Tomcat 10, con la
  base de datos ya creada y `application.properties` apuntando a ella.

## Variables de entorno recomendadas para produccion
```
CLAVE_CIFRADO=...
WOMPI_LLAVE_PUBLICA=...
WOMPI_LLAVE_INTEGRIDAD=...
WOMPI_LLAVE_EVENTOS=...
GEMINI_API_KEY=...
```

## Verificacion post-despliegue
1. La pagina de inicio carga y muestra el catalogo.
2. Se puede iniciar sesion con cada uno de los cinco roles.
3. El panel administrativo muestra los seis indicadores con datos reales.
4. Un pedido de prueba completa el ciclo: compra, pago, aceptacion, despacho, entrega.
