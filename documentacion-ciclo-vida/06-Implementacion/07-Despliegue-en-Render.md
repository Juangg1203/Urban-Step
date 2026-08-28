# 7. Despliegue en Render (con Docker)

Render no tiene un entorno nativo para Java (a diferencia de Node o Python), asi que
este proyecto se despliega con el `Dockerfile` que esta en la raiz del repositorio.
Ese Dockerfile compila con Maven en una etapa y corre con un JRE liviano en otra.

> Aviso: Render cambia su oferta con frecuencia. Verifica en `render.com/docs` si algo
> de lo siguiente ya cambio antes de seguir estos pasos al pie de la letra.

## 1. Base de datos MySQL externa

Render no ofrece MySQL administrado (solo PostgreSQL), asi que se necesita un MySQL
en otro lado. Opciones gratuitas comunes para proyectos academicos: Aiven, Railway,
Clever Cloud, o un XAMPP con un tunel publico (menos recomendable para algo permanente).

Una vez tengas el host, el usuario, la clave y el nombre de la base:
1. Crea la base de datos vacia.
2. Ejecuta `basedatos/01_esquema.sql` contra ella (con un cliente MySQL que se pueda
   conectar de forma remota, o el que te de el proveedor).

## 2. Crear el servicio en Render

1. New → Web Service → conecta el repositorio de GitHub.
2. En **Environment**, elige **Docker** (no Node ni el detectado automaticamente).
   Render encontrara el `Dockerfile` en la raiz solo.
3. Region: la que quede mas cerca de tus usuarios (o cualquiera, para un proyecto
   academico).
4. Deja **Build Command** y **Start Command** vacios: el `Dockerfile` ya define como
   se compila y como se arranca.

## 3. Variables de entorno

En la seccion Environment del servicio, agrega:

| Variable | Valor |
|---|---|
| `DB_URL` | `jdbc:mysql://TU_HOST:3306/tienda_ropa?useSSL=true&serverTimezone=America/Bogota&characterEncoding=UTF-8` |
| `DB_USUARIO` | El usuario de tu MySQL externo |
| `DB_CLAVE` | Su clave |
| `CLAVE_CIFRADO` | Una frase larga y unica (no la de desarrollo local) |
| `WOMPI_URL_RETORNO` | `https://TU-SERVICIO.onrender.com/pagos/wompi/retorno` (ajusta cuando sepas la URL final) |

Las de Wompi y Gemini (`WOMPI_LLAVE_*`, `GEMINI_API_KEY`) son opcionales; sin ellas la
app sigue funcionando con la pasarela en modo simulado y el chatbot con reglas.

## 4. Desplegar

Con eso, "Deploy web service". La primera compilacion con Maven dentro de Docker
tarda varios minutos; los siguientes despliegues son mas rapidos por el cache de capas.

## Limitacion importante: las imagenes subidas no persisten

Las imagenes que un Jefe sube desde el panel (`Panel → Productos`) se guardan en el
disco del contenedor (`app.imagenes.carpeta`). En el plan gratuito de Render, ese disco
**no es persistente**: cada vez que el servicio se reinicia o se vuelve a desplegar,
esas imagenes se pierden (las que vienen empacadas en `recursos/img/productos/`, en
cambio, si persisten, porque son parte del codigo, no de lo subido).

Para que las imagenes subidas sobrevivan en produccion, hacen falta uno de estos dos:
- Un **disco persistente** de Render (funcionalidad de pago), apuntando
  `app.imagenes.carpeta` a esa ruta montada.
- Guardarlas en un servicio externo (Cloudinary, S3) en vez de en disco local — eso
  implicaria cambiar `ImagenService` para subir ahi en vez de a una carpeta.

Para una entrega academica, esta limitacion se puede simplemente documentar y mostrar
con el catalogo de demostracion (que ya trae imagenes empacadas en el codigo).
