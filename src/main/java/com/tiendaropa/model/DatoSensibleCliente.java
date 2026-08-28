package com.tiendaropa.model;

import java.time.LocalDateTime;
import com.tiendaropa.util.ConvertidorCifrado;
import jakarta.persistence.*;

/**
 * NIVEL SENSIBLE. Cifrado y sujeto a autorizacion expresa y revocable.
 * Ningun rol interno lo puede leer: solo el titular. La empresa solo lo
 * usa de forma agregada y anonima (por ejemplo, curva de tallas).
 */
@Entity
@Table(name = "dato_sensible_cliente")
public class DatoSensibleCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false, unique = true)
    private Cliente cliente;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "medidas_corporales", length = 600)
    private String medidasCorporales;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "alergias_materiales", length = 600)
    private String alergiasMateriales;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "condicion_movilidad", length = 600)
    private String condicionMovilidad;

    @Convert(converter = ConvertidorCifrado.class)
    @Column(name = "restriccion_vestimenta", length = 600)
    private String restriccionVestimenta;

    @Column(nullable = false)
    private boolean autorizado = false;

    @Column(name = "fecha_autorizacion")
    private LocalDateTime fechaAutorizacion;

    public DatoSensibleCliente() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public String getMedidasCorporales() { return medidasCorporales; }
    public void setMedidasCorporales(String medidasCorporales) { this.medidasCorporales = medidasCorporales; }
    public String getAlergiasMateriales() { return alergiasMateriales; }
    public void setAlergiasMateriales(String alergiasMateriales) { this.alergiasMateriales = alergiasMateriales; }
    public String getCondicionMovilidad() { return condicionMovilidad; }
    public void setCondicionMovilidad(String condicionMovilidad) { this.condicionMovilidad = condicionMovilidad; }
    public String getRestriccionVestimenta() { return restriccionVestimenta; }
    public void setRestriccionVestimenta(String restriccionVestimenta) { this.restriccionVestimenta = restriccionVestimenta; }
    public boolean isAutorizado() { return autorizado; }
    public void setAutorizado(boolean autorizado) { this.autorizado = autorizado; }
    public LocalDateTime getFechaAutorizacion() { return fechaAutorizacion; }
    public void setFechaAutorizacion(LocalDateTime fechaAutorizacion) { this.fechaAutorizacion = fechaAutorizacion; }
}
