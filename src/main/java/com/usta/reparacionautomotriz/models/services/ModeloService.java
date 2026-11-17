package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.ModeloEntity;

import java.util.List;

public interface ModeloService {
    List<ModeloEntity> findAll();
    ModeloEntity findById(Long id);
    void save(ModeloEntity modelo);
    void update(ModeloEntity modelo);
    void changeState(Long id);
}
