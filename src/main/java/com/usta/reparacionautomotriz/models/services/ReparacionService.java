package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.ReparacionEntity;
import com.usta.reparacionautomotriz.enums.EstadoReparacion;

import java.util.List;

public interface ReparacionService {
    List<ReparacionEntity> findAll();
    List<ReparacionEntity> findByMecanico(Long idUsuario);
    ReparacionEntity findById(Long id);
    void save(ReparacionEntity reparacion);
    void update(ReparacionEntity reparacion);
    void cambiarEstado(Long id, EstadoReparacion nuevoEstado);
}
