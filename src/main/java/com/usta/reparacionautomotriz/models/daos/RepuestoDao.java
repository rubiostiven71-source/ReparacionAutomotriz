package com.usta.reparacionautomotriz.models.daos;

import com.usta.reparacionautomotriz.entities.RepuestoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepuestoDao extends JpaRepository<RepuestoEntity, Long> {
    boolean existsByCodigoEan(String codigoEan);
    boolean existsByCodigoEanAndIdRepuestoNot(String codigoEan, Long idRepuesto);

    // búsqueda B1: repuestos activos compatibles con un modelo
    List<RepuestoEntity> findByModelos_IdModeloAndEstadoRepuestoTrue(Long idModelo);
}
