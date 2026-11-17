package com.usta.reparacionautomotriz.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class RoleEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    // Valores: ADMINISTRADOR, MECANICO
    @NotBlank
    @Size(max = 20)
    @Column(name = "nombre_rol", nullable = false, unique = true, length = 20)
    private String rol;

    public RoleEntity(String rol) {
        this.rol = rol;
    }
}
