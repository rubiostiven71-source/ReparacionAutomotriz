package com.usta.reparacionautomotriz.entities;

import com.usta.reparacionautomotriz.enums.ColorPlaca;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "vehiculos")
public class VehiculoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Long idVehiculo;

    @NotBlank(message = "{vehiculo.placa.obligatoria}")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{3}$", message = "{vehiculo.placa.formato}")  // RN-01
    @Column(name = "placa", nullable = false, unique = true, length = 7)
    private String placa;

    @NotNull(message = "{vehiculo.color.obligatorio}")                                 // RN-02
    @Enumerated(EnumType.STRING)
    @Column(name = "color_placa", nullable = false, length = 10)
    private ColorPlaca colorPlaca;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre_dueno", nullable = false, length = 100)
    private String nombreDueno;

    @NotBlank
    @Size(max = 20)
    @Column(name = "documento_dueno", nullable = false, length = 20)
    private String documentoDueno;

    @NotBlank
    @Size(max = 20)
    @Column(name = "telefono_dueno", nullable = false, length = 20)
    private String telefonoDueno;

    @Column(name = "estado_vehiculo", nullable = false)
    private boolean estadoVehiculo = true;

    @NotNull(message = "{vehiculo.modelo.obligatorio}")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_modelo", nullable = false)
    private ModeloEntity modelo;

    // Normaliza a mayúsculas ANTES de que Bean Validation revise el formato (RN-01)
    public void setPlaca(String placa) {
        this.placa = (placa == null) ? null : placa.trim().toUpperCase();
    }
}
