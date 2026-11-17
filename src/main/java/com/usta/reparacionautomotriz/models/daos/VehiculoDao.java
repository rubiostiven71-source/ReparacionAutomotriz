package com.usta.reparacionautomotriz.models.daos;

import com.usta.reparacionautomotriz.entities.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehiculoDao extends JpaRepository<VehiculoEntity, Long> {
    Optional<VehiculoEntity> findByPlaca(String placa);          // búsqueda B2
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdVehiculoNot(String placa, Long idVehiculo);
    List<VehiculoEntity> findByEstadoVehiculoTrue();
}
