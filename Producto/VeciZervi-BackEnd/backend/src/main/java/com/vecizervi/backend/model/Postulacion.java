package com.vecizervi.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "postulaciones")
public class Postulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_postulacion")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_trabajo", nullable = false)
    @JsonIgnoreProperties({"cliente","categoria","descripcion","postulaciones","mensajes"})
    private Trabajo trabajo;

    @ManyToOne
    @JoinColumn(name = "id_trabajador", nullable = false)
    @JsonIgnoreProperties({"password","tokenRecuperacion","cuentaBloqueadaHasta","intentosFallidos","herramientasPropias"})
    private Usuario trabajador;

    @Column(name = "mensaje_presentacion", columnDefinition = "TEXT")
    private String mensajePresentacion;

    @CreationTimestamp
    @Column(name = "fecha_postulacion", updatable = false)
    private LocalDateTime fechaPostulacion;

    public Postulacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Trabajo getTrabajo() { return trabajo; }
    public void setTrabajo(Trabajo trabajo) { this.trabajo = trabajo; }
    public Usuario getTrabajador() { return trabajador; }
    public void setTrabajador(Usuario trabajador) { this.trabajador = trabajador; }
    public String getMensajePresentacion() { return mensajePresentacion; }
    public void setMensajePresentacion(String m) { this.mensajePresentacion = m; }
    public LocalDateTime getFechaPostulacion() { return fechaPostulacion; }
}