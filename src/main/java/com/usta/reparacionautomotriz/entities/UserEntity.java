package com.usta.reparacionautomotriz.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "usuarios")
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long idUsuario;

    @NotBlank
    @Size(max = 60)
    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @NotBlank
    @Size(max = 60)
    @Column(name = "apellido", nullable = false, length = 60)
    private String apellido;

    @NotBlank
    @Size(max = 20)
    @Column(name = "telefono", nullable = false, length = 20)
    private String telefono;

    @NotBlank
    @Email
    @Size(max = 100)
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    // Aquí se guarda el hash BCrypt (60 caracteres), nunca la clave en texto plano (RN-12)
    @NotBlank
    @Column(name = "clave", nullable = false, length = 60)
    private String clave;

    @Column(name = "estado_usu", nullable = false)
    private boolean estadoUsu = true;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_rol", nullable = false)
    private RoleEntity rol;
}
