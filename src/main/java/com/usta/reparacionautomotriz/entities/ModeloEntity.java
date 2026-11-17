package com.usta.reparacionautomotriz.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "modelos",
        uniqueConstraints = @UniqueConstraint(name = "uk_modelo_marca_nombre",
                columnNames = {"marca", "nombre_modelo"}))          // RN-04

public class ModeloEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modelo")
    private Long idModelo;

    @NotBlank(message = "{modelo.nombre.obligatorio}")
    @Size(max = 100, message = "{campo.max100}")
    @Column(name = "nombre_modelo", nullable = false, length = 100)
    private String nombreModelo;

    @NotBlank(message = "{modelo.marca.obligatoria}")
    @Size(max = 60, message = "{campo.max60}")
    @Column(name = "marca", nullable = false, length = 60)
    private String marca;

    // true = activo, false = dado de baja (RN-11)
    @Column(name = "estado_modelo", nullable = false)
    private boolean estadoModelo = true;
}
