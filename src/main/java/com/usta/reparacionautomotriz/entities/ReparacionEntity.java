package com.usta.reparacionautomotriz.entities;

import com.usta.reparacionautomotriz.enums.EstadoReparacion;
import com.usta.reparacionautomotriz.enums.TipoReparacion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "reparaciones")
public class ReparacionEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reparacion")
    private Long idReparacion;

    @NotNull(message = "{reparacion.fecha.obligatoria}")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @NotNull(message = "{reparacion.tipo.obligatorio}")                    // RN-09
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private TipoReparacion tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 15)
    private EstadoReparacion estado = EstadoReparacion.EN_PROCESO;            // RN-08

    @Size(max = 500)
    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @NotNull(message = "{reparacion.vehiculo.obligatorio}")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_vehiculo", nullable = false)
    private VehiculoEntity vehiculo;

    // Mecánico responsable (RN-10)
    @NotNull(message = "{reparacion.mecanico.obligatorio}")
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private UserEntity mecanico;
}
