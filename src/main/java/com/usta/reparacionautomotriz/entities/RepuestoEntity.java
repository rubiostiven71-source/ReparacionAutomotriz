package com.usta.reparacionautomotriz.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "repuestos")
public class RepuestoEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repuesto")
    private Long idRepuesto;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "{repuesto.precio.obligatorio}")
    @DecimalMin(value = "0.0", inclusive = false, message = "{repuesto.precio.positivo}")   // RN-06
    @Digits(integer = 10, fraction = 2)
    @Column(name = "precio", nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @NotBlank
    @Pattern(regexp = "^[0-9]{13}$", message = "{repuesto.ean.formato}")          // RN-05
    @Column(name = "codigo_ean", nullable = false, unique = true, length = 13)
    private String codigoEan;

    @NotNull
    @PositiveOrZero(message = "{repuesto.stock.negativo}")                                      // RN-06
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Size(max = 200)
    @Column(name = "imagen", length = 200)
    private String imagen;

    @Column(name = "estado_repuesto", nullable = false)
    private boolean estadoRepuesto = true;

    // N:M con modelos (RN-07). Sin cascade: un repuesto nunca debe borrar ni crear modelos.
    @NotEmpty(message = "{repuesto.modelos.obligatorio}")
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "repuestos_modelos",
            joinColumns = @JoinColumn(name = "id_repuesto"),
            inverseJoinColumns = @JoinColumn(name = "id_modelo"))
    private Set<ModeloEntity> modelos = new HashSet<>();
}
