package com.vecizervi.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_trabajo", nullable = false)
    @JsonIgnoreProperties({
        "cliente","categoria","descripcion","postulaciones",
        "mensajes","estado","precio","comuna","fechaPublicacion",
        "fechaFinalizacion","latitud","longitud","titulo"
    })
    private Trabajo trabajo;

    @ManyToOne
    @JoinColumn(name = "id_emisor", nullable = false)
    @JsonIgnoreProperties({
        "password","tokenRecuperacion","cuentaBloqueadaHasta",
        "intentosFallidos","herramientasPropias","rut",
        "fechaNacimiento","correo","rol","calificacionPromedio"
    })
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "id_receptor")
    @JsonIgnoreProperties({
        "password","tokenRecuperacion","cuentaBloqueadaHasta",
        "intentosFallidos","herramientasPropias","rut",
        "fechaNacimiento","correo","rol","calificacionPromedio"
    })
    private Usuario receptor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio;

    // FIX-29/30: campo para saber si el receptor ya leyÃ³ el mensaje
    @Column(name = "leido", nullable = false)
    private boolean leido = false;

    public Mensaje() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Trabajo getTrabajo() { return trabajo; }
    public void setTrabajo(Trabajo trabajo) { this.trabajo = trabajo; }
    public Usuario getEmisor() { return emisor; }
    public void setEmisor(Usuario emisor) { this.emisor = emisor; }
    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }
    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
    public void setFechaEnvio(LocalDateTime f) { this.fechaEnvio = f; }
    public boolean isLeido() { return leido; }
    public void setLeido(boolean leido) { this.leido = leido; }
}