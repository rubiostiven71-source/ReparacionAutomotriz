package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.ModeloEntity;
import com.usta.reparacionautomotriz.entities.RepuestoEntity;
import com.usta.reparacionautomotriz.exceptions.EanDuplicadoException;
import com.usta.reparacionautomotriz.models.daos.ModeloDao;
import com.usta.reparacionautomotriz.models.daos.RepuestoDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RepuestoServiceImp implements RepuestoService {

    public static final String REGEX_EAN = "^[0-9]{13}$";   // RN-05

    private final RepuestoDao repuestoDao;
    private final ModeloDao modeloDao;

    public RepuestoServiceImp(RepuestoDao repuestoDao, ModeloDao modeloDao) {
        this.repuestoDao = repuestoDao;
        this.modeloDao = modeloDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RepuestoEntity> findAll() {
        return repuestoDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public RepuestoEntity findById(Long id) {
        return repuestoDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(RepuestoEntity repuesto) {
        validarReglas(repuesto);
        if (repuestoDao.existsByCodigoEan(repuesto.getCodigoEan())) {
            throw new EanDuplicadoException("Ya existe un repuesto con el EAN " + repuesto.getCodigoEan());
        }
        repuesto.setModelos(cargarModelos(repuesto.getModelos()));
        repuesto.setEstadoRepuesto(true);
        repuestoDao.save(repuesto);
    }

    @Override
    @Transactional
    public void update(RepuestoEntity repuesto) {
        RepuestoEntity actual = repuestoDao.findById(repuesto.getIdRepuesto())
                .orElseThrow(() -> new IllegalArgumentException("Repuesto no encontrado"));
        validarReglas(repuesto);
        if (repuestoDao.existsByCodigoEanAndIdRepuestoNot(repuesto.getCodigoEan(), repuesto.getIdRepuesto())) {
            throw new EanDuplicadoException("Ya existe un repuesto con el EAN " + repuesto.getCodigoEan());
        }
        actual.setNombre(repuesto.getNombre().trim());
        actual.setDescripcion(repuesto.getDescripcion());
        actual.setPrecio(repuesto.getPrecio());
        actual.setCodigoEan(repuesto.getCodigoEan());
        actual.setStock(repuesto.getStock());
        actual.setImagen(repuesto.getImagen());
        actual.setModelos(cargarModelos(repuesto.getModelos()));
        repuestoDao.save(actual);
    }

    @Override
    @Transactional
    public void changeState(Long id) {
        RepuestoEntity repuesto = repuestoDao.findById(id).orElse(null);
        if (repuesto != null) {
            repuesto.setEstadoRepuesto(!repuesto.isEstadoRepuesto());
            repuestoDao.save(repuesto);
        }
    }

    // Búsqueda B1: repuestos activos compatibles con el modelo (relación N:M)
    @Override
    @Transactional(readOnly = true)
    public List<RepuestoEntity> findCompatibles(Long idModelo) {
        return repuestoDao.findByModelos_IdModeloAndEstadoRepuestoTrue(idModelo);
    }

    // RN-05 y RN-06 se validan también en el servicio (no solo con Bean Validation)
    private void validarReglas(RepuestoEntity repuesto) {
        String ean = (repuesto.getCodigoEan() == null) ? "" : repuesto.getCodigoEan().trim();
        if (!ean.matches(REGEX_EAN)) {
            throw new IllegalArgumentException("El EAN debe tener exactamente 13 dígitos");
        }
        repuesto.setCodigoEan(ean);
        if (repuesto.getPrecio() == null || repuesto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor que cero");
        }
        if (repuesto.getStock() == null || repuesto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
        if (repuesto.getModelos() == null || repuesto.getModelos().isEmpty()) {
            throw new IllegalArgumentException("Seleccione al menos un modelo compatible");
        }
    }

    // Reemplaza los modelos que llegan del formulario por los de la base de datos (RN-07)
    private Set<ModeloEntity> cargarModelos(Set<ModeloEntity> seleccionados) {
        Set<ModeloEntity> modelos = new HashSet<>();
        for (ModeloEntity m : seleccionados) {
            modelos.add(modeloDao.findById(m.getIdModelo())
                    .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado")));
        }
        return modelos;
    }
}
