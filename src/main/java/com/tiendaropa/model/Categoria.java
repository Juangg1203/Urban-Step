package com.tiendaropa.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String linea; // ROPA | CALZADO

    public Categoria() { }
    public Categoria(String nombre, String linea) { this.nombre = nombre; this.linea = linea; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getLinea() { return linea; }
    public void setLinea(String linea) { this.linea = linea; }
}
