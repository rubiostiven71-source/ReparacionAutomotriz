package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.ModeloEntity;
import com.usta.reparacionautomotriz.entities.VehiculoEntity;
import com.usta.reparacionautomotriz.exceptions.PlacaDuplicadaException;
import com.usta.reparacionautomotriz.exceptions.PlacaInvalidaException;
import com.usta.reparacionautomotriz.models.daos.ModeloDao;
import com.usta.reparacionautomotriz.models.daos.VehiculoDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VehiculoServiceImp implements VehiculoService {

    // RN-01: tres letras mayúsculas, guion y tres dígitos
    public static final String REGEX_PLACA = "^[A-Z]{3}-[0-9]{3}$";

    private final VehiculoDao vehiculoDao;
    private final ModeloDao modeloDao;

    public VehiculoServiceImp(VehiculoDao vehiculoDao, ModeloDao modeloDao) {
        this.vehiculoDao = vehiculoDao;
        this.modeloDao = modeloDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoEntity> findAll() {
        return vehiculoDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<VehiculoEntity> findActivos() {
        return vehiculoDao.findByEstadoVehiculoTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public VehiculoEntity findById(Long id) {
        return vehiculoDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(VehiculoEntity vehiculo) {
        String placa = validarFormatoPlaca(vehiculo.getPlaca());
        if (vehiculoDao.existsByPlaca(placa)) {
            throw new PlacaDuplicadaException("La placa " + placa + " ya está registrada");
        }
        vehiculo.setPlaca(placa);
        vehiculo.setModelo(buscarModeloActivo(vehiculo.getModelo()));
        vehiculo.setEstadoVehiculo(true);
        vehiculoDao.save(vehiculo);
    }

    @Override
    @Transactional
    public void update(VehiculoEntity vehiculo) {
        VehiculoEntity actual = vehiculoDao.findById(vehiculo.getIdVehiculo())
                .orElseThrow(() -> new IllegalArgumentException("Vehículo no encontrado"));
        String placa = validarFormatoPlaca(vehiculo.getPlaca());
        if (vehiculoDao.existsByPlacaAndIdVehiculoNot(placa, vehiculo.getIdVehiculo())) {
            throw new PlacaDuplicadaException("La placa " + placa + " ya está registrada");
        }
        // Solo se copian los campos editables; el estado no cambia por el formulario
        actual.setPlaca(placa);
        actual.setColorPlaca(vehiculo.getColorPlaca());
        actual.setNombreDueno(vehiculo.getNombreDueno().trim());
        actual.setDocumentoDueno(vehiculo.getDocumentoDueno().trim());
        actual.setTelefonoDueno(vehiculo.getTelefonoDueno().trim());
        actual.setModelo(buscarModeloActivo(vehiculo.getModelo()));
        vehiculoDao.save(actual);
    }

    @Override
    @Transactional
    public void changeState(Long id) {
        VehiculoEntity vehiculo = vehiculoDao.findById(id).orElse(null);
        if (vehiculo != null) {
            vehiculo.setEstadoVehiculo(!vehiculo.isEstadoVehiculo());
            vehiculoDao.save(vehiculo);
        }
    }

    // Búsqueda B2: valida el formato ANTES de consultar la base de datos.
    // Devuelve null si la placa no está registrada.
    @Override
    @Transactional(readOnly = true)
    public VehiculoEntity findByPlaca(String placa) {
        String normalizada = validarFormatoPlaca(placa);
        return vehiculoDao.findByPlaca(normalizada).orElse(null);
    }

    // Normaliza a mayúsculas y valida el formato ABC-123 (RN-01)
    private String validarFormatoPlaca(String placa) {
        String normalizada = (placa == null) ? "" : placa.trim().toUpperCase();
        if (!normalizada.matches(REGEX_PLACA)) {
            throw new PlacaInvalidaException("Formato de placa inválido. Use ABC-123");
        }
        return normalizada;
    }

    // El vehículo debe quedar asociado a un modelo existente y activo
    private ModeloEntity buscarModeloActivo(ModeloEntity modelo) {
        if (modelo == null || modelo.getIdModelo() == null) {
            throw new IllegalArgumentException("Seleccione un modelo");
        }
        ModeloEntity encontrado = modeloDao.findById(modelo.getIdModelo())
                .orElseThrow(() -> new IllegalArgumentException("Modelo no encontrado"));
        if (!encontrado.isEstadoModelo()) {
            throw new IllegalArgumentException("El modelo seleccionado está dado de baja");
        }
        return encontrado;
    }
}
