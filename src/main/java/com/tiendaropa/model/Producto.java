package com.tiendaropa.model;

import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String sku;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(length = 120)
    private String tallas;

    @Column(length = 60)
    private String color;

    @Column(length = 80)
    private String material;

    /**
     * Nombre del archivo subido (se sirve desde /imagenes/**) o una URL
     * completa si la imagen esta alojada afuera. Se guarda la referencia,
     * no la imagen: meter binarios en la base la infla sin necesidad.
     */
    @Column(length = 300)
    private String imagen;

    @Column(nullable = false)
    private int stock;

    /** Umbral de alerta. Por debajo de esto el panel avisa que hay que reponer. */
    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo = 5;

    @Column(nullable = false)
    private boolean activo = true;

    public Producto() { }

    public Producto(String sku, String nombre, String descripcion, Categoria categoria,
                    BigDecimal precio, String tallas, String color, String material, int stock) {
        this.sku = sku;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.precio = precio;
        this.tallas = tallas;
        this.color = color;
        this.material = material;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    /** Ruta lista para el atributo src, o vacio si no hay imagen. */
    public String getRutaImagen() {
        if (imagen == null || imagen.isBlank()) return "";
        // http(s): imagen externa. Empieza por "/": ya es una ruta completa
        // (por ejemplo una imagen de muestra empacada en el proyecto, no
        // subida desde el panel). Cualquier otro caso: es el nombre de un
        // archivo que subio el panel y vive en /imagenes/**.
        if (imagen.startsWith("http://") || imagen.startsWith("https://") || imagen.startsWith("/")) {
            return imagen;
        }
        return "/imagenes/" + imagen;
    }
    public boolean isTieneImagen() { return imagen != null && !imagen.isBlank(); }

    /** Estado legible: lo que se muestra en el panel y en el catalogo. */
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    /** true cuando conviene reponer: queda poco pero todavia hay. */
    public boolean isStockBajo() { return activo && stock > 0 && stock <= stockMinimo; }

    public String getEstado() {
        if (!activo) return "Inactivo";
        if (stock <= 0) return "Agotado";
        return isStockBajo() ? "Quedan pocos" : "Disponible";
    }
    public boolean isDisponible() { return activo && stock > 0; }

    public void setId(Long id) { this.id = id; }
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getTallas() { return tallas; }
    public void setTallas(String tallas) { this.tallas = tallas; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
