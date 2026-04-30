package com.vecizervi.backend.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String rut;

    private String nombres;
    private String apellidos;
    private LocalDate fechaNacimiento;

    @Column(unique = true, nullable = false)
    private String correo;

    @Column(nullable = false)
    private String contrasenaEnCriptada; 
}
