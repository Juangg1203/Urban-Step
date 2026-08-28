# 1. Arquitectura del sistema

Monolito en capas sobre Spring Boot 3, con vistas JSP renderizadas en el servidor.
Se eligio esta arquitectura y no un frontend separado con API REST porque el proyecto
lo despliega y mantiene un equipo pequeno: una sola aplicacion es mas simple de
ejecutar, depurar y sustentar, y el sistema no necesita servir a varios clientes
distintos (movil nativo, otro sitio, etc.).

Ver `02-Diagrama-Arquitectura.png` para el recorrido completo de una peticion, capa por
capa: navegador -> Spring Security -> controlador -> servicio -> repositorio -> MySQL.

El diagrama de componentes (`05-Diagrama-Componentes.png`) es la misma vista de capas,
leida como componentes desplegables en vez de como el recorrido de una peticion: son
dos lecturas del mismo corte arquitectonico, no dos arquitecturas distintas.
