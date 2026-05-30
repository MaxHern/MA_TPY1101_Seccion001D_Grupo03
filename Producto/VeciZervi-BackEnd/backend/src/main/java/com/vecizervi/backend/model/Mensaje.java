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
    // Solo devuelve el id del trabajo, evita ciclo infinito
    @JsonIgnoreProperties({"cliente","categoria","descripcion","postulaciones","mensajes"})
    private Trabajo trabajo;

    @ManyToOne
    @JoinColumn(name = "id_emisor", nullable = false)
    // Solo devuelve datos bÃ¡sicos del emisor
    @JsonIgnoreProperties({"password","tokenRecuperacion","cuentaBloqueadaHasta","intentosFallidos","herramientasPropias"})
    private Usuario emisor;

    @ManyToOne
    @JoinColumn(name = "id_receptor")
    @JsonIgnoreProperties({"password","tokenRecuperacion","cuentaBloqueadaHasta","intentosFallidos","herramientasPropias"})
    private Usuario receptor;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @CreationTimestamp
    @Column(name = "fecha_envio", updatable = false)
    private LocalDateTime fechaEnvio;

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
}