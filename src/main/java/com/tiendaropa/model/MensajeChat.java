package com.tiendaropa.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "mensaje_chat")
public class MensajeChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversacion_id", nullable = false)
    private Conversacion conversacion;

    @Column(nullable = false, length = 10)
    private String emisor; // CLIENTE | BOT | AGENTE

    @Column(nullable = false, length = 2000)
    private String texto;

    @Column(length = 40)
    private String intencion;

    @Column(name = "respondido_ia", nullable = false)
    private boolean respondidoIa = false;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    public MensajeChat() { }

    public MensajeChat(Conversacion conversacion, String emisor, String texto) {
        this.conversacion = conversacion;
        this.emisor = emisor;
        this.texto = texto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Conversacion getConversacion() { return conversacion; }
    public void setConversacion(Conversacion conversacion) { this.conversacion = conversacion; }
    public String getEmisor() { return emisor; }
    public void setEmisor(String emisor) { this.emisor = emisor; }
    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }
    public String getIntencion() { return intencion; }
    public void setIntencion(String intencion) { this.intencion = intencion; }
    public boolean isRespondidoIa() { return respondidoIa; }
    public void setRespondidoIa(boolean respondidoIa) { this.respondidoIa = respondidoIa; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
