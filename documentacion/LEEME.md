# Documentacion del proyecto

| Archivo | Que es |
|---|---|
| `documento-funcionalidades.docx` / `.pdf` | Documento de funcionalidades implementadas (18 paginas) |
| `diagrama-entidad-relacion.png` / `.svg` | Modelo entidad-relacion con claves primarias y foraneas |
| `diagrama-arquitectura.png` / `.svg` | Arquitectura en capas y recorrido de una peticion |
| `generar_diagrama_er.py` | Script que genera el diagrama ER |
| `generar_diagrama_arquitectura.py` | Script que genera el diagrama de arquitectura |

## Por que los diagramas se generan con codigo

Un diagrama dibujado a mano en una herramienta grafica queda desactualizado en
cuanto cambia una tabla, y nadie se acuerda de corregirlo. Estos se describen en
un script: si manana se agrega un campo, se edita la lista y se regenera.

Para regenerarlos hace falta Graphviz instalado:

```bash
pip install graphviz
python documentacion/generar_diagrama_er.py
python documentacion/generar_diagrama_arquitectura.py
```

Se entregan en PNG (para pegar en un documento) y en SVG (para ampliar sin que
se pixele o para editar en Figma o Inkscape).

## Lo que falta por hacer

- Capturas de pantalla de la aplicacion funcionando.
- Video corto de demostracion, si el docente lo pide.
