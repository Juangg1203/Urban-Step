package com.tiendaropa.service;

import java.text.Normalizer;
import java.time.LocalTime;
import java.util.List;
import com.tiendaropa.model.Tema;

/**
 * Reglas del chatbot. Funciona sin internet y sin llaves de API:
 * es la primera capa de respuesta y cubre las preguntas frecuentes.
 * Lo que no encaje aqui pasa a la IA (si esta habilitada) o al agente.
 *
 * Cada regla trae ademas sus propias sugerencias de seguimiento, para que
 * la conversacion siga sola en vez de terminar en cada respuesta.
 */
public final class BaseConocimiento {

    public static class Regla {
        private final String intencion;
        private final Tema tema;
        private final List<String> claves;
        private final String respuesta;
        private final List<String> sugerencias;

        public Regla(String intencion, Tema tema, List<String> claves, String respuesta,
                     List<String> sugerencias) {
            this.intencion = intencion;
            this.tema = tema;
            this.claves = claves;
            this.respuesta = respuesta;
            this.sugerencias = sugerencias;
        }
        public String getIntencion() { return intencion; }
        public Tema getTema() { return tema; }
        public List<String> getClaves() { return claves; }
        public String getRespuesta() { return respuesta; }
        public List<String> getSugerencias() { return sugerencias; }
    }

    /** Lo que se ofrece al abrir el chat o cuando no se entiende la pregunta. */
    public static final List<String> SUGERENCIAS_INICIALES = List.of(
            "Como se cual es mi talla?", "Donde va mi pedido?",
            "Como hago un cambio?", "Quien puede ver mis datos?");

    public static final List<Regla> REGLAS = List.of(

        // ---------------- conversacion basica ----------------
        new Regla("SALUDO", Tema.OTRO,
            List.of("hola", "buenas", "buenos dias", "buenas tardes", "buenas noches", "que tal", "hey",
                    "alo", "saludos", "buen dia"),
            "Hola, soy Aguja, el asistente de UrbanStep. Te ayudo con tallas, envios, cambios, "
          + "pagos, el estado de tu pedido y con tus datos personales. Cuentame que necesitas.",
            SUGERENCIAS_INICIALES),

        new Regla("COMO_ESTAS", Tema.OTRO,
            List.of("como estas", "como vas", "como te sientes", "que haces", "quien eres", "eres un robot",
                    "eres humano", "eres una persona", "eres real", "como te llamas"),
            "Soy Aguja, un asistente automatico de UrbanStep, asi que no soy una persona: funciono con "
          + "reglas y con un modelo de lenguaje. Estoy disponible las 24 horas y si en algun momento "
          + "prefieres hablar con alguien del equipo, escribe \"asesor\" y paso tu caso.",
            List.of("Que puedes hacer?", "Quiero hablar con un asesor", "Ver el catalogo")),

        new Regla("CAPACIDADES", Tema.OTRO,
            List.of("que puedes hacer", "en que me ayudas", "que sabes", "ayuda", "opciones", "menu",
                    "para que sirves", "no se que preguntar"),
            "Puedo ayudarte con seis cosas: encontrar tu talla, consultar el catalogo y precios, revisar "
          + "envios y el estado de un pedido, tramitar cambios y devoluciones, explicarte los medios de "
          + "pago, y resolver dudas sobre como tratamos tus datos personales. Elige una o preguntame "
          + "directo.",
            List.of("Como se cual es mi talla?", "Donde va mi pedido?",
                    "Medios de pago", "Quien puede ver mis datos?")),

        // ---------------- producto y tallas ----------------
        new Regla("TALLAS", Tema.TALLAS,
            List.of("talla", "tallas", "medida", "medidas", "numero de calzado", "que talla", "queda grande",
                    "queda pequeno", "guia de tallas", "s m l", "calza", "mi talla", "tallaje"),
            "Nuestra guia de tallas esta en cada ficha de producto. En ropa manejamos XS a XXL y en calzado "
          + "del 34 al 45. Si nos guardas tus medidas en Mi cuenta (dato sensible, cifrado y solo visible "
          + "para ti), el sitio te sugiere la talla sin que nadie del equipo vea tus medidas.",
            List.of("Estoy entre dos tallas", "La ropa encoge al lavarla?",
                    "Como hago un cambio?", "Ver el catalogo")),

        new Regla("ENTRE_TALLAS", Tema.TALLAS,
            List.of("entre dos tallas", "estoy entre", "cual me sirve", "me queda flojo", "me queda apretado",
                    "talla intermedia", "una talla mas"),
            "Si estas entre dos tallas, la recomendacion general es subir una en calzado y en prendas de "
          + "tejido plano (jeans, chaquetas), y quedarte con la menor en tejidos con elastano. Cada ficha "
          + "indica si la horma es ajustada o suelta.",
            List.of("La ropa encoge al lavarla?", "Como hago un cambio?", "Ver el catalogo")),

        new Regla("CUIDADO_PRENDA", Tema.PRODUCTO,
            List.of("encoge", "lavar", "lavado", "planchar", "destine", "se decolora", "cuidado de la prenda",
                    "secadora", "como lavo"),
            "Nuestras prendas de algodon vienen preencogidas, asi que la variacion despues del lavado es "
          + "minima. Recomendamos lavar en frio, del reves y sin secadora para el algodon y el denim. Cada "
          + "prenda trae su etiqueta con las instrucciones exactas del material.",
            List.of("De que material son?", "Como hago un cambio?", "Ver el catalogo")),

        new Regla("PRODUCTO", Tema.PRODUCTO,
            List.of("catalogo", "precio", "cuanto cuesta", "cuanto vale", "disponible", "stock", "color",
                    "material", "tenis", "camiseta", "pantalon", "botas", "chaqueta", "sandalias",
                    "algodon", "cuero", "que venden", "que tienen", "de que material"),
            "Puedes ver todo el catalogo en la seccion Catalogo, con filtro por linea (ropa o calzado) y por "
          + "categoria. Cada ficha trae material, colores, tallas disponibles y existencias en tiempo real. "
          + "Dime que producto buscas y te oriento.",
            List.of("Que me recomiendas?", "Tienen descuentos?", "Como se cual es mi talla?")),

        new Regla("RECOMENDACION", Tema.PRODUCTO,
            List.of("que me recomiendas", "recomiendame", "para un matrimonio", "para una fiesta",
                    "para el trabajo", "para hacer ejercicio", "formal", "elegante", "regalo",
                    "para clima frio", "para clima caliente", "que me pongo"),
            "Con gusto te oriento. Para ocasiones formales tenemos chaquetas y pantalones de linea; para "
          + "diario, camisetas de algodon y tenis; y para clima frio, chaquetas con forro. Dime la ocasion "
          + "y el presupuesto y te sugiero productos concretos del catalogo.",
            List.of("Ver el catalogo", "Tienen descuentos?", "Como se cual es mi talla?")),

        new Regla("PROMOCIONES", Tema.PROMOCIONES,
            List.of("descuento", "promocion", "oferta", "rebaja", "cupon", "codigo", "barato", "economico",
                    "no tengo dinero", "esta caro", "algo mas barato", "outlet"),
            "Manejamos descuentos por temporada que aparecen marcados en el catalogo, y el envio es gratis "
          + "desde $180.000. No tenemos cupones permanentes, pero si te registras y autorizas las "
          + "comunicaciones comerciales, te avisamos cuando bajen de precio los productos que te interesan. "
          + "Esa autorizacion la puedes revocar cuando quieras.",
            List.of("Ver el catalogo", "Como me registro?", "Medios de pago")),

        // ---------------- pedidos, envios, pagos ----------------
        new Regla("ENVIOS", Tema.ENVIOS,
            List.of("envio", "envios", "domicilio", "entrega", "cuanto demora", "cuanto tarda",
                    "transportadora", "flete", "llega", "costo de envio", "envian a"),
            "Enviamos a todo el pais. En ciudades principales la entrega toma de 1 a 3 dias habiles y en el "
          + "resto del pais de 3 a 6. Los pedidos superiores a $180.000 tienen envio gratis. Cuando el pedido "
          + "sale de bodega te llega el numero de guia al correo.",
            List.of("Donde va mi pedido?", "Medios de pago", "Puedo cambiar la direccion?")),

        new Regla("ESTADO_PEDIDO", Tema.PEDIDO,
            List.of("mi pedido", "donde va", "rastrear", "seguimiento", "guia", "ya lo despacharon",
                    "cuando llega mi", "estado del pedido", "numero de orden", "no ha llegado"),
            "Puedes seguir tu pedido desde Mi cuenta: alli ves el estado en cada paso, desde que se "
          + "confirma el pago hasta que sale de bodega y llega a tu direccion. Si ya paso la fecha estimada "
          + "y sigue sin llegar, escribe \"asesor\" y abrimos el caso con la transportadora.",
            List.of("Cuanto demora el envio?", "Puedo cambiar la direccion?",
                    "Quiero hablar con un asesor")),

        new Regla("CANCELAR_PEDIDO", Tema.PEDIDO,
            List.of("cancelar el pedido", "cancelar mi compra", "anular", "me arrepenti", "ya no lo quiero",
                    "cambiar la direccion", "modificar el pedido", "puedo cambiar la direccion"),
            "Mientras el pedido no haya salido de bodega puedes cancelarlo o corregir la direccion sin "
          + "costo desde Mi cuenta. Si ya fue despachado, toca esperar la entrega y tramitarlo como "
          + "devolucion, que tambien esta cubierta.",
            List.of("Como hago un cambio?", "Donde va mi pedido?", "Quiero hablar con un asesor")),

        new Regla("PAGOS", Tema.PAGOS,
            List.of("pago", "pagar", "tarjeta", "pse", "efectivo", "contra entrega", "factura", "cuotas",
                    "medios de pago", "transferencia", "nequi", "daviplata", "como pago"),
            "Recibimos tarjeta debito y credito, PSE, transferencia y pago contra entrega en ciudades "
          + "principales. Los datos de la tarjeta los procesa la pasarela de pagos: la tienda nunca los "
          + "guarda en su base de datos.",
            List.of("Es seguro pagar aqui?", "Cuanto demora el envio?", "Tienen descuentos?")),

        new Regla("SEGURIDAD_PAGO", Tema.PAGOS,
            List.of("es seguro", "seguridad", "me van a robar", "confiable", "estafa", "guardan mi tarjeta",
                    "es confiable la pagina", "roban datos", "es seguro pagar"),
            "El pago lo procesa una pasarela certificada, no nosotros: la tienda nunca ve ni almacena el "
          + "numero de tu tarjeta. Tus datos personales privados y sensibles se guardan cifrados, y cada "
          + "consulta que hace un empleado a esa informacion queda registrada en una auditoria.",
            List.of("Quien puede ver mis datos?", "Medios de pago", "Que clave debo usar?")),

        new Regla("DEVOLUCIONES", Tema.DEVOLUCIONES,
            List.of("devolver", "devolucion", "cambio", "cambiar", "garantia", "retracto", "no me quedo",
                    "defectuoso", "reembolso", "me quedo mal", "vino danado", "llego roto"),
            "Tienes 5 dias habiles para retractarte y 30 dias para cambio de talla, con la prenda sin uso y "
          + "con etiquetas. El primer cambio de talla no tiene costo de envio. Si el producto llego "
          + "defectuoso, la garantia cubre el cambio completo sin costo para ti.",
            List.of("Quiero hablar con un asesor", "Donde va mi pedido?",
                    "Como se cual es mi talla?")),

        // ---------------- cuenta y datos ----------------
        new Regla("REGISTRO", Tema.CUENTA,
            List.of("registrarme", "crear cuenta", "como me registro", "abrir cuenta", "no tengo cuenta",
                    "necesito cuenta para comprar", "registro"),
            "Crear la cuenta es gratis y toma un minuto: necesitas nombre, correo y una clave segura. Los "
          + "datos privados (documento, direccion, telefono) los agregas tu despues, y los sensibles como "
          + "tus medidas solo si tu autorizas. Puedes navegar el catalogo sin cuenta, pero para comprar y "
          + "hacer seguimiento si la necesitas.",
            List.of("Que clave debo usar?", "Quien puede ver mis datos?", "Puedo borrar mi cuenta?")),

        new Regla("CLAVE", Tema.CUENTA,
            List.of("clave", "contrasena", "password", "olvide mi clave", "recuperar clave", "no puedo entrar",
                    "cambiar clave", "clave segura", "no me deja ingresar", "bloqueado", "que clave debo usar"),
            "Tu clave se guarda cifrada con BCrypt: ni el equipo tecnico puede leerla. Para que sea segura "
          + "pide minimo 8 caracteres, con mayusculas, minusculas y numeros; el formulario te muestra en "
          + "vivo que tan fuerte es. Si no puedes entrar, escribe \"asesor\" y verificamos tu identidad "
          + "antes de restablecerla.",
            List.of("Quien puede ver mis datos?", "Quiero hablar con un asesor", "Como me registro?")),

        new Regla("DATOS", Tema.DATOS,
            List.of("datos personales", "habeas data", "privacidad", "politica de datos", "eliminar mi cuenta",
                    "borrar mis datos", "quien ve mis datos", "proteccion de datos", "autorizacion", "cedula",
                    "revocar", "para que quieren mis datos", "mis datos", "quien puede ver mis datos"),
            "Tus datos se clasifican en cuatro niveles: publicos, semiprivados, privados y sensibles. Los "
          + "privados y sensibles se guardan cifrados. Ningun empleado, ni el administrador, puede ver tus "
          + "datos sensibles: solo tu. Desde Mi cuenta puedes consultarlos, corregirlos, revocar la "
          + "autorizacion o pedir su eliminacion.",
            List.of("Que son datos sensibles?", "Puedo borrar mi cuenta?", "Es seguro pagar aqui?")),

        new Regla("DATOS_SENSIBLES", Tema.DATOS,
            List.of("datos sensibles", "que son sensibles", "mis medidas", "por que piden mis medidas",
                    "alergias", "movilidad", "no quiero dar mis medidas", "que son datos sensibles"),
            "Los datos sensibles son los que podrian usarse para discriminarte: en nuestro caso, tus medidas "
          + "corporales, alergias a materiales y condiciones de movilidad. Son opcionales, sirven solo para "
          + "recomendarte tallas, se guardan cifrados y ningun empleado puede verlos. Si revocas la "
          + "autorizacion, se eliminan en ese momento; no los conservamos.",
            List.of("Quien puede ver mis datos?", "Puedo borrar mi cuenta?", "Como se cual es mi talla?")),

        new Regla("ELIMINAR_CUENTA", Tema.DATOS,
            List.of("borrar mi cuenta", "eliminar cuenta", "darme de baja", "no quiero mas correos",
                    "quitar mis datos", "cancelar suscripcion", "no me escriban", "puedo borrar mi cuenta"),
            "Puedes revocar la autorizacion de comunicaciones comerciales o pedir la eliminacion de tus "
          + "datos desde Mi cuenta, sin dar explicaciones. Conservamos unicamente lo que la ley obliga a "
          + "guardar de las compras facturadas; el resto se elimina.",
            List.of("Quien puede ver mis datos?", "Que son datos sensibles?",
                    "Quiero hablar con un asesor")),

        // ---------------- servicio ----------------
        new Regla("HORARIO", Tema.OTRO,
            List.of("horario", "horarios", "abierto", "atienden", "sede", "tienda fisica", "direccion",
                    "telefono", "donde quedan", "contacto", "whatsapp", "estan abiertos"),
            "El chat responde las 24 horas, todos los dias. Los asesores humanos atienden de lunes a sabado "
          + "de 8:00 a 18:00. Nuestra tienda esta en la Calle 36 # 22-15, Bucaramanga, y el WhatsApp de "
          + "atencion es 300 000 0000.",
            List.of("Quiero hablar con un asesor", "Cuanto demora el envio?", "Ver el catalogo")),

        new Regla("QUEJA", Tema.OTRO,
            List.of("pesimo", "malisimo", "terrible", "estoy molesto", "estoy furioso", "no sirve",
                    "mal servicio", "es una verguenza", "llevo esperando", "nadie me responde", "estafadores"),
            "Lamento que hayas tenido esa experiencia, y tienes razon en reclamar. Prefiero que esto lo vea "
          + "una persona del equipo: voy a escalar tu caso ahora mismo para que le den respuesta. Si me "
          + "cuentas el numero de pedido, lo adjunto al reporte.",
            List.of()),

        new Regla("AGENTE", Tema.OTRO,
            List.of("asesor", "humano", "persona", "hablar con alguien", "agente", "no me sirve",
                    "quiero hablar", "reclamo", "queja", "hablar con un asesor", "atencion humana"),
            "Con gusto. Voy a pasar tu caso a un asesor humano; en horario de atencion te responden por "
          + "correo el mismo dia.",
            List.of()),

        new Regla("DESPEDIDA", Tema.OTRO,
            List.of("gracias", "muchas gracias", "listo", "chao", "adios", "hasta luego", "eso era todo",
                    "nada mas", "perfecto gracias"),
            "Con gusto. Antes de cerrar, cuentame como estuvo la atencion: tu calificacion es la que alimenta "
          + "el reporte mensual con el que mejoramos el servicio.",
            List.of())
    );

    private BaseConocimiento() { }

    /** Saludo distinto segun la hora, para que el chat no suene igual siempre. */
    public static String saludoPorHora(String nombre) {
        int hora = LocalTime.now().getHour();
        String momento = hora < 12 ? "Buenos dias" : (hora < 19 ? "Buenas tardes" : "Buenas noches");
        String quien = (nombre == null || nombre.isBlank()) ? "" : " " + nombre;
        return momento + quien + ", soy Aguja, el asistente de UrbanStep. En que te ayudo?";
    }

    /** Quita tildes y pasa a minusculas para poder comparar. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String limpio = Normalizer.normalize(texto.toLowerCase().trim(), Normalizer.Form.NFD);
        return limpio.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    /** Devuelve la regla con mas coincidencias de palabras clave, o null. */
    public static Regla buscar(String mensaje) {
        String texto = normalizar(mensaje);
        if (texto.length() < 2) return null;
        Regla mejor = null;
        int mejorPuntaje = 0;
        for (Regla regla : REGLAS) {
            int puntaje = 0;
            for (String clave : regla.getClaves()) {
                if (texto.contains(normalizar(clave))) {
                    puntaje += clave.contains(" ") ? 2 : 1; // las frases pesan mas
                }
            }
            if (puntaje > mejorPuntaje) {
                mejorPuntaje = puntaje;
                mejor = regla;
            }
        }
        return mejor;
    }
}
