# Ciclo de vida del desarrollo — UrbanStep

Organización del proyecto según las seis fases del ciclo de vida del software. Cada
carpeta tiene su propio documento; esta página solo es el mapa.

| Fase | Contiene |
|---|---|
| [00_ingenieria-de-requerimientos](00_ingenieria-de-requerimientos/requisitos.md) | Enunciado original, requisitos funcionales y no funcionales, con ID trazable |
| [01_analisis](01_analisis/analisis.md) | Actores, reglas de negocio, casos de uso e historias de usuario |
| [02_diseno](02_diseno/diseno.md) | Diagrama entidad-relación, diagrama de arquitectura, identidad visual |
| [03_desarrollo](03_desarrollo/desarrollo.md) | Stack, convenciones de código, fases de construcción (el código vive en `src/`) |
| [04_pruebas](04_pruebas/plan-de-pruebas.md) | 22 casos de prueba manuales trazables a requisitos, con espacio para evidencia |
| [05_implementacion](05_implementacion/) | Manual de instalación y manual de usuario por rol |

El código fuente ejecutable está en la raíz del proyecto (`src/`, `pom.xml`,
`basedatos/`), no duplicado dentro de estas carpetas, para que el repositorio siga
siendo un único proyecto Maven compilable.
