package com.tiendaropa.service;

import com.tiendaropa.dto.Acceso;
import com.tiendaropa.model.NivelDato;
import com.tiendaropa.model.Rol;
import org.springframework.stereotype.Service;

/**
 * Matriz de acceso por nivel de dato. Es el unico lugar donde se decide
 * quien ve que; los controladores y las vistas solo consultan el resultado.
 *
 *  Nivel        | Titular | Empleado    | Jefe          | Administrador
 *  -------------|---------|-------------|---------------|--------------
 *  PUBLICO      | total   | total       | total         | total
 *  SEMIPRIVADO  | total   | total       | total         | total
 *  PRIVADO      | total   | enmascarado | total         | NINGUNO
 *  SENSIBLE     | total   | ninguno     | solo metadato | NINGUNO
 *
 * Dos decisiones que conviene poder defender:
 *
 * 1. El administrador queda fuera de los niveles privado y sensible a
 *    proposito. Administrar el sistema no exige leer la cedula, la direccion
 *    ni las medidas de un cliente. Menos personas con acceso, menos fuga.
 *
 * 2. El jefe hereda la funcion de oficial de proteccion de datos: ve los
 *    datos privados completos y audita los accesos, porque es quien responde
 *    por el tratamiento. Aun asi, del nivel sensible solo ve el metadato
 *    (si existe y desde cuando), nunca el contenido.
 */
@Service
public class PoliticaAccesoService {

    public Acceso evaluar(Rol rol, NivelDato nivel, boolean esTitular) {
        if (esTitular) return Acceso.COMPLETO;
        if (rol == null) return Acceso.DENEGADO;

        switch (nivel) {
            case PUBLICO:
                return Acceso.COMPLETO;

            case SEMIPRIVADO:
                return (rol == Rol.EMPLEADO || rol == Rol.JEFE || rol == Rol.ADMIN)
                        ? Acceso.COMPLETO : Acceso.DENEGADO;

            case PRIVADO:
                if (rol == Rol.JEFE) return Acceso.COMPLETO;
                if (rol == Rol.EMPLEADO) return Acceso.ENMASCARADO;
                return Acceso.DENEGADO;   // incluye ADMIN

            case SENSIBLE:
                // Ningun rol interno lee el contenido. El jefe solo ve si
                // existe y desde cuando esta autorizado.
                return Acceso.DENEGADO;

            default:
                return Acceso.DENEGADO;
        }
    }

    /** El jefe puede auditar la existencia del dato sensible, no su contenido. */
    public boolean puedeVerMetadatoSensible(Rol rol, boolean esTitular) {
        return esTitular || rol == Rol.JEFE;
    }
}
