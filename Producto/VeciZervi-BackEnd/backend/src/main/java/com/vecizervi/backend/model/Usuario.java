package com.vecizervi.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

// FIX-22/S03: ocultar RUT, correo y campos internos en todas las respuestas JSON públicas
@JsonIgnoreProperties({
    "rut",
    "correo",
    "contrasenaEnCriptada",
    "tokenRecuperacion",
    "cuentaBloqueadaHasta",
    "intentosFallidos"
})
@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @JsonProperty("idUsuario")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut;

    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;

    @Column(unique = true, nullable = false)
    private String correo;

    @JsonProperty("password")
    @Column(name = "password")
    private String contrasenaEnCriptada;

    private Integer intentosFallidos = 0;
    private LocalDateTime cuentaBloqueadaHasta;
    private String tokenRecuperacion;

    @Column(name = "rol")
    private String rol = "USER";

    @Column(name = "calificacion_promedio")
    private Double calificacionPromedio = 0.0;
}