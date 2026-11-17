package com.usta.reparacionautomotriz.models.services;

import com.usta.reparacionautomotriz.entities.ReparacionEntity;
import com.usta.reparacionautomotriz.entities.UserEntity;
import com.usta.reparacionautomotriz.entities.VehiculoEntity;
import com.usta.reparacionautomotriz.enums.EstadoReparacion;
import com.usta.reparacionautomotriz.exceptions.ReparacionInvalidaException;
import com.usta.reparacionautomotriz.models.daos.ReparacionDao;
import com.usta.reparacionautomotriz.models.daos.UserDao;
import com.usta.reparacionautomotriz.models.daos.VehiculoDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReparacionServiceImp implements ReparacionService {

    public static final String ROL_MECANICO = "MECANICO";

    private final ReparacionDao reparacionDao;
    private final VehiculoDao vehiculoDao;
    private final UserDao userDao;

    public ReparacionServiceImp(ReparacionDao reparacionDao, VehiculoDao vehiculoDao, UserDao userDao) {
        this.reparacionDao = reparacionDao;
        this.vehiculoDao = vehiculoDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReparacionEntity> findAll() {
        return reparacionDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReparacionEntity> findByMecanico(Long idUsuario) {
        return reparacionDao.findByMecanico_IdUsuario(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public ReparacionEntity findById(Long id) {
        return reparacionDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void save(ReparacionEntity reparacion) {
        reparacion.setVehiculo(buscarVehiculoActivo(reparacion.getVehiculo()));
        reparacion.setMecanico(buscarMecanico(reparacion.getMecanico()));
        if (reparacion.getFecha() == null) {
            reparacion.setFecha(LocalDate.now());
        }
        reparacion.setEstado(EstadoReparacion.EN_PROCESO);   // RN-08: toda reparación nace EN_PROCESO
        reparacionDao.save(reparacion);
    }

    @Override
    @Transactional
    public void update(ReparacionEntity reparacion) {
        ReparacionEntity actual = reparacionDao.findById(reparacion.getIdReparacion())
                .orElseThrow(() -> new IllegalArgumentException("Reparación no encontrada"));
        if (actual.getEstado() != EstadoReparacion.EN_PROCESO) {
            throw new ReparacionInvalidaException("Solo se editan reparaciones EN_PROCESO");
        }
        // El estado no se cambia por el formulario; se usa cambiarEstado()
        actual.setFecha(reparacion.getFecha());
        actual.setTipo(reparacion.getTipo());
        actual.setObservaciones(reparacion.getObservaciones());
        actual.setVehiculo(buscarVehiculoActivo(reparacion.getVehiculo()));
        actual.setMecanico(buscarMecanico(reparacion.getMecanico()));
        reparacionDao.save(actual);
    }

    // RN-08: EN_PROCESO -> TERMINADO o CANCELADO. Los estados finales no vuelven atrás.
    @Override
    @Transactional
    public void cambiarEstado(Long id, EstadoReparacion nuevoEstado) {
        ReparacionEntity reparacion = reparacionDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reparación no encontrada"));
        if (reparacion.getEstado() != EstadoReparacion.EN_PROCESO) {
            throw new ReparacionInvalidaException("La reparación ya está " + reparacion.getEstado()
                    + " y no puede cambiar de estado");
        }
        if (nuevoEstado != EstadoReparacion.TERMINADO && nuevoEstado != EstadoReparacion.CANCELADO) {
            throw new ReparacionInvalidaException("Estado destino no permitido: " + nuevoEstado);
        }
        reparacion.setEstado(nuevoEstado);
        reparacionDao.save(reparacion);
    }

    // El vehículo debe existir y estar activo
    private VehiculoEntity buscarVehiculoActivo(VehiculoEntity vehiculo) {
        if (vehiculo == null || vehiculo.getIdVehiculo() == null) {
            throw new ReparacionInvalidaException("Seleccione un vehículo");
        }
        VehiculoEntity encontrado = vehiculoDao.findById(vehiculo.getIdVehiculo())
                .orElseThrow(() -> new ReparacionInvalidaException("Vehículo no encontrado"));
        if (!encontrado.isEstadoVehiculo()) {
            throw new ReparacionInvalidaException("El vehículo " + encontrado.getPlaca() + " está dado de baja");
        }
        return encontrado;
    }

    // RN-10: el responsable debe ser un usuario activo con rol MECANICO
    private UserEntity buscarMecanico(UserEntity mecanico) {
        if (mecanico == null || mecanico.getIdUsuario() == null) {
            throw new ReparacionInvalidaException("Seleccione un mecánico");
        }
        UserEntity encontrado = userDao.findById(mecanico.getIdUsuario())
                .orElseThrow(() -> new ReparacionInvalidaException("Mecánico no encontrado"));
        if (!encontrado.isEstadoUsu() || encontrado.getRol() == null
                || !ROL_MECANICO.equals(encontrado.getRol().getRol())) {
            throw new ReparacionInvalidaException("El usuario seleccionado no es un mecánico activo");
        }
        return encontrado;
    }
}
