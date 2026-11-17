package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.ModeloEntity;
import com.usta.reparacionautomotriz.exceptions.ModeloDuplicadoException;
import com.usta.reparacionautomotriz.models.daos.ModeloDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ModeloServiceImp implements ModeloService {

    private final ModeloDao modeloDao;

    public ModeloServiceImp(ModeloDao modeloDao) {
        this.modeloDao = modeloDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModeloEntity> findAll() {
        return modeloDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public ModeloEntity findById(Long id) {
        return modeloDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ModeloEntity modelo) {
        normalizar(modelo);
        if (modeloDao.existsByMarcaIgnoreCaseAndNombreModeloIgnoreCase(
                modelo.getMarca(), modelo.getNombreModelo())) {
            throw new ModeloDuplicadoException("Ya existe el modelo "
                    + modelo.getNombreModelo() + " de la marca " + modelo.getMarca());
        }
        modelo.setEstadoModelo(true);
        modeloDao.save(modelo);
    }

    @Override
    @Transactional
    public void update(ModeloEntity modelo) {
        normalizar(modelo);
        ModeloEntity actual = modeloDao.findById(modelo.getIdModelo())
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado"));
        if (modeloDao.existsByMarcaIgnoreCaseAndNombreModeloIgnoreCaseAndIdModeloNot(
                modelo.getMarca(), modelo.getNombreModelo(), modelo.getIdModelo())) {
            throw new ModeloDuplicadoException("Ya existe el modelo "
                    + modelo.getNombreModelo() + " de la marca " + modelo.getMarca());
        }
        // Solo se copian los campos editables; el estado no cambia por el formulario
        actual.setNombreModelo(modelo.getNombreModelo());
        actual.setMarca(modelo.getMarca());
        modeloDao.save(actual);
    }

    @Override
    @Transactional
    public void changeState(Long id) {
        ModeloEntity modelo = modeloDao.findById(id).orElse(null);
        if (modelo != null) {
            modelo.setEstadoModelo(!modelo.isEstadoModelo());
            modeloDao.save(modelo);
        }
    }

    private void normalizar(ModeloEntity modelo) {
        modelo.setNombreModelo(modelo.getNombreModelo().trim());
        modelo.setMarca(modelo.getMarca().trim());
    }
}
