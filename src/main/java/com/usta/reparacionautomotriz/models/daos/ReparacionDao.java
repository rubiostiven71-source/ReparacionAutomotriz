package com.usta.reparacionautomotriz.models.daos;

import com.usta.reparacionautomotriz.entities.ReparacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReparacionDao extends JpaRepository<ReparacionEntity, Long> {
    List<ReparacionEntity> findByVehiculo_IdVehiculo(Long idVehiculo);
    List<ReparacionEntity> findByMecanico_IdUsuario(Long idUsuario);
}
