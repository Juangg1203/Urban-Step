# 1. Plan de implementacion

## Orden sugerido de puesta en marcha
1. Preparar el motor de base de datos (MySQL/MariaDB).
2. Crear la base con `02-Manual-de-Instalacion.md`.
3. Ajustar `application.properties` con las credenciales reales.
4. Compilar y arrancar la aplicacion.
5. Verificar el ingreso con los cinco usuarios de prueba (uno por rol).
6. Configurar, si se van a usar, la IA del chatbot y la pasarela de pagos en modo real.

## Responsables (contexto academico)
Un solo desarrollador cubrio todo el ciclo; en un entorno real, la implementacion en
produccion (paso 6 en adelante, ver `03-Configuracion-Produccion.md`) la haria quien
administra la infraestructura, no el mismo equipo de desarrollo.

## Riesgos identificados
- Cambios de esquema entre fases del proyecto: si se reutiliza una base de datos
  antigua, hay que recrearla (ver la nota en el manual de instalacion).
- El modo simulado de la pasarela de pagos no debe usarse en produccion real.
