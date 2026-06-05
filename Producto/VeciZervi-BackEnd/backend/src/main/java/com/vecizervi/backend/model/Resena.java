package com.vecizervi.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "resenas")
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_trabajo", nullable = false)
    // Solo devolver id y titulo del trabajo para evitar ciclos
    @JsonIgnoreProperties({
        "cliente","categoria","descripcion","postulaciones",
        "mensajes","estado","precio","comuna","fechaPublicacion",
        "fechaFinalizacion","latitud","longitud"
    })
    private Trabajo trabajo;

    @ManyToOne
    @JoinColumn(name = "id_emisor", nullable = false)
    @JsonIgnoreProperties({
        "password","tokenRecuperacion","cuentaBloqueadaHasta",
        "intentosFallidos","herramientasPropias","calificacionPromedio",
        "rut","fechaNacimiento","correo","rol"
    })
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "id_receptor", nullable = false)
    @JsonIgnoreProperties({
        "password","tokenRecuperacion","cuentaBloqueadaHasta",
        "intentosFallidos","herramientasPropias","calificacionPromedio",
        "rut","fechaNacimiento","correo","rol"
    })
    private Usuario receptor;

    @Column(nullable = false)
    private Integer estrellas;

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "url_foto_evidencia")
    private String urlFotoEvidencia;

    public Resena() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Trabajo getTrabajo() { return trabajo; }
    public void setTrabajo(Trabajo trabajo) { this.trabajo = trabajo; }
    public Usuario getEmisor() { return emisor; }
    public void setEmisor(Usuario emisor) { this.emisor = emisor; }
    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }
    public Integer getEstrellas() { return estrellas; }
    public void setEstrellas(Integer estrellas) { this.estrellas = estrellas; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
    public String getUrlFotoEvidencia() { return urlFotoEvidencia; }
    public void setUrlFotoEvidencia(String url) { this.urlFotoEvidencia = url; }
}