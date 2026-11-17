package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.RepuestoEntity;

import java.util.List;

public interface RepuestoService {
    List<RepuestoEntity> findAll();
    RepuestoEntity findById(Long id);
    void save(RepuestoEntity repuesto);
    void update(RepuestoEntity repuesto);
    void changeState(Long id);
    List<RepuestoEntity> findCompatibles(Long idModelo);   // búsqueda B1
}
