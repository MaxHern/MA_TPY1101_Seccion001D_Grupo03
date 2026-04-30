package com.vecizervi.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trabajos")
public class Trabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titulo;
    private String descripcion;
    private Double precio;
    private String categoria;
    private String estado = "DISPONIBLE"; 
    
    private LocalDateTime fechaPublicacion = LocalDateTime.now();
    private LocalDateTime fechaFinalizacion;
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;
}